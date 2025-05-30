/*
 *  Copyright (c) 2018 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <memory>
#include <optional>
#include <utility>

#include "api/rtp_packet_infos.h"
#include "api/video/encoded_image.h"
#include "api/video/video_codec_type.h"
#include "api/video/video_content_type.h"
#include "api/video/video_frame_type.h"
#include "api/video/video_rotation.h"
#include "api/video/video_timing.h"
#include "modules/rtp_rtcp/source/frame_object.h"
#include "modules/rtp_rtcp/source/rtp_video_header.h"
#include "modules/video_coding/codecs/h264/include/h264_globals.h"
#include "modules/video_coding/codecs/vp8/include/vp8_globals.h"
#include "modules/video_coding/codecs/vp9/include/vp9_globals.h"
#include "modules/video_coding/rtp_frame_reference_finder.h"

namespace webrtc {

namespace {
class DataReader {
 public:
  DataReader(const uint8_t* data, size_t size) : data_(data), size_(size) {}

  template <typename T>
  void CopyTo(T* object) {
    static_assert(std::is_trivial_v<T> && std::is_standard_layout_v<T>, "");
    uint8_t* destination = reinterpret_cast<uint8_t*>(object);
    size_t object_size = sizeof(T);
    size_t num_bytes = std::min(size_ - offset_, object_size);
    memcpy(destination, data_ + offset_, num_bytes);
    offset_ += num_bytes;

    // If we did not have enough data, fill the rest with 0.
    object_size -= num_bytes;
    memset(destination + num_bytes, 0, object_size);
  }

  template <typename T>
  T GetNum() {
    T res;
    if (offset_ + sizeof(res) < size_) {
      memcpy(&res, data_ + offset_, sizeof(res));
      offset_ += sizeof(res);
      return res;
    }

    offset_ = size_;
    return T(0);
  }

  bool MoreToRead() { return offset_ < size_; }

 private:
  const uint8_t* data_;
  size_t size_;
  size_t offset_ = 0;
};

RTPVideoHeaderH264 GenerateRTPVideoHeaderH264(DataReader* reader) {
  RTPVideoHeaderH264 result;
  result.nalu_type = reader->GetNum<uint8_t>();
  result.packetization_type = reader->GetNum<H264PacketizationTypes>();
  int nalus_length = reader->GetNum<uint8_t>();
  for (int i = 0; i < nalus_length; ++i) {
    reader->CopyTo(&result.nalus.emplace_back());
  }
  result.packetization_mode = reader->GetNum<H264PacketizationMode>();
  return result;
}

std::optional<RTPVideoHeader::GenericDescriptorInfo>
GenerateGenericFrameDependencies(DataReader* reader) {
  std::optional<RTPVideoHeader::GenericDescriptorInfo> result;
  uint8_t flags = reader->GetNum<uint8_t>();
  if (flags & 0b1000'0000) {
    // i.e. with 50% chance there are no generic dependencies.
    // in such case codec-specfic code path of the RtpFrameReferenceFinder will
    // be validated.
    return result;
  }

  result.emplace();
  result->frame_id = reader->GetNum<int32_t>();
  result->spatial_index = (flags & 0b0111'0000) >> 4;
  result->temporal_index = (flags & 0b0000'1110) >> 1;

  // Larger than supported by the RtpFrameReferenceFinder.
  int num_diffs = (reader->GetNum<uint8_t>() % 16);
  for (int i = 0; i < num_diffs; ++i) {
    result->dependencies.push_back(result->frame_id -
                                   (reader->GetNum<uint16_t>() % (1 << 14)));
  }

  return result;
}
}  // namespace

void FuzzOneInput(const uint8_t* data, size_t size) {
  DataReader reader(data, size);
  RtpFrameReferenceFinder reference_finder;

  auto codec = static_cast<VideoCodecType>(reader.GetNum<uint8_t>() % 5);

  while (reader.MoreToRead()) {
    uint16_t first_seq_num = reader.GetNum<uint16_t>();
    uint16_t last_seq_num = reader.GetNum<uint16_t>();
    bool marker_bit = reader.GetNum<uint8_t>();

    RTPVideoHeader video_header;
    switch (reader.GetNum<uint8_t>() % 3) {
      case 0:
        video_header.frame_type = VideoFrameType::kEmptyFrame;
        break;
      case 1:
        video_header.frame_type = VideoFrameType::kVideoFrameKey;
        break;
      case 2:
        video_header.frame_type = VideoFrameType::kVideoFrameDelta;
        break;
    }

    switch (codec) {
      case kVideoCodecVP8:
        reader.CopyTo(
            &video_header.video_type_header.emplace<RTPVideoHeaderVP8>());
        break;
      case kVideoCodecVP9:
        reader.CopyTo(
            &video_header.video_type_header.emplace<RTPVideoHeaderVP9>());
        break;
      case kVideoCodecH264:
        video_header.video_type_header = GenerateRTPVideoHeaderH264(&reader);
        break;
      case kVideoCodecH265:
        // TODO(bugs.webrtc.org/13485)
        break;
      default:
        break;
    }

    video_header.generic = GenerateGenericFrameDependencies(&reader);

    // clang-format off
    auto frame = std::make_unique<RtpFrameObject>(
        first_seq_num,
        last_seq_num,
        marker_bit,
        /*times_nacked=*/0,
        /*first_packet_received_time=*/0,
        /*last_packet_received_time=*/0,
        /*rtp_timestamp=*/0,
        /*ntp_time_ms=*/0,
        VideoSendTiming(),
        /*payload_type=*/0,
        codec,
        kVideoRotation_0,
        VideoContentType::UNSPECIFIED,
        video_header,
        /*color_space=*/std::nullopt,
        /*frame_instrumentation_data=*/std::nullopt,
        RtpPacketInfos(),
        EncodedImageBuffer::Create(/*size=*/0));
    // clang-format on

    reference_finder.ManageFrame(std::move(frame));
  }
}

}  // namespace webrtc
