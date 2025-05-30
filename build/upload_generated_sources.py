#!/usr/bin/env/python
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

import argparse
import gzip
import io
import logging
import os
import sys
import tarfile
import time
from contextlib import contextmanager
from queue import Queue
from threading import Event, Thread

import requests
from mozbuild.generated_sources import (
    get_filename_with_digest,
    get_s3_region_and_bucket,
)
from requests.packages.urllib3.util.retry import Retry

# Arbitrary, should probably measure this.
NUM_WORKER_THREADS = 10
log = logging.getLogger("upload-generated-sources")
log.setLevel(logging.INFO)


@contextmanager
def timed():
    """
    Yield a function that provides the elapsed time in seconds since this
    function was called.
    """
    start = time.time()

    def elapsed():
        return time.time() - start

    yield elapsed


def gzip_compress(data):
    """
    Apply gzip compression to `data` and return the result as a `BytesIO`.
    """
    b = io.BytesIO()
    with gzip.GzipFile(fileobj=b, mode="w") as f:
        f.write(data)
    b.flush()
    b.seek(0)
    return b


def upload_worker(queue, event, bucket, session_args):
    """
    Get `(name, contents)` entries from `queue` and upload `contents`
    to S3 with gzip compression using `name` as the key, prefixed with
    the SHA-512 digest of `contents` as a hex string. If an exception occurs,
    set `event`.
    """
    try:
        import boto3

        session = boto3.session.Session(**session_args)
        s3 = session.client("s3")
        while True:
            if event.is_set():
                # Some other thread hit an exception.
                return
            (name, contents) = queue.get()
            pathname = get_filename_with_digest(name, contents)
            compressed = gzip_compress(contents)
            extra_args = {
                "ContentEncoding": "gzip",
                "ContentType": "text/plain",
            }
            log.info(f'Uploading "{pathname}" ({len(compressed.getvalue())} bytes)')
            with timed() as elapsed:
                s3.upload_fileobj(compressed, bucket, pathname, ExtraArgs=extra_args)
                log.info(f'Finished uploading "{pathname}" in {elapsed():0.3f}s')
            queue.task_done()
    except Exception:
        log.exception("Thread encountered exception:")
        event.set()


def do_work(artifact, region, bucket):
    session_args = {"region_name": region}
    session = requests.Session()
    retry = Retry(total=5, backoff_factor=0.1, status_forcelist=[500, 502, 503, 504])
    http_adapter = requests.adapters.HTTPAdapter(max_retries=retry)
    session.mount("https://", http_adapter)
    session.mount("http://", http_adapter)

    if "TASK_ID" in os.environ:
        level = os.environ.get("MOZ_SCM_LEVEL", "1")
        secrets_url = "http://taskcluster/secrets/v1/secret/project/releng/gecko/build/level-{}/gecko-generated-sources-upload".format(  # noqa
            level
        )
        log.info(f'Using AWS credentials from the secrets service: "{secrets_url}"')
        res = session.get(secrets_url)
        res.raise_for_status()
        secret = res.json()
        session_args.update(
            aws_access_key_id=secret["secret"]["AWS_ACCESS_KEY_ID"],
            aws_secret_access_key=secret["secret"]["AWS_SECRET_ACCESS_KEY"],
        )
    else:
        log.info("Trying to use your AWS credentials..")

    # First, fetch the artifact containing the sources.
    log.info(f'Fetching generated sources artifact: "{artifact}"')
    with timed() as elapsed:
        res = session.get(artifact)
        log.info(
            f"Fetch HTTP status: {res.status_code}, {len(res.content)} bytes downloaded in {elapsed():0.3f}s"
        )
    res.raise_for_status()
    # Create a queue and worker threads for uploading.
    q = Queue()
    event = Event()
    log.info(f"Creating {NUM_WORKER_THREADS} worker threads")
    for i in range(NUM_WORKER_THREADS):
        t = Thread(target=upload_worker, args=(q, event, bucket, session_args))
        t.daemon = True
        t.start()
    with tarfile.open(fileobj=io.BytesIO(res.content), mode="r|gz") as tar:
        # Next, process each file.
        for entry in tar:
            if event.is_set():
                break
            log.info(f'Queueing "{entry.name}"')
            q.put((entry.name, tar.extractfile(entry).read()))
    # Wait until all uploads are finished.
    # We don't use q.join() here because we want to also monitor event.
    while q.unfinished_tasks:
        if event.wait(0.1):
            log.error("Worker thread encountered exception, exiting...")
            sys.exit(1)


def main(argv):
    logging.basicConfig(format="%(levelname)s - %(threadName)s - %(message)s")
    parser = argparse.ArgumentParser(
        description="Upload generated source files in ARTIFACT to BUCKET in S3."
    )
    parser.add_argument("artifact", help="generated-sources artifact from build task")
    args = parser.parse_args(argv)
    region, bucket = get_s3_region_and_bucket()

    with timed() as elapsed:
        do_work(region=region, bucket=bucket, artifact=args.artifact)
        log.info(f"Finished in {elapsed():.03f}s")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
