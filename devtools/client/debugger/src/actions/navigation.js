/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at <http://mozilla.org/MPL/2.0/>. */

import sourceQueue from "../utils/source-queue";

import { clearWasmStates } from "../utils/wasm";
import { getMainThread } from "../selectors/index";
import { evaluateExpressionsForCurrentContext } from "../actions/expressions";

import { features } from "../utils/prefs";
import { getEditor } from "../utils/editor/index";

/**
 * Redux actions for the navigation state
 * @module actions/navigation
 */

/**
 * @memberof actions/navigation
 * @static
 */
export function willNavigate(event) {
  return async function ({ dispatch, getState, sourceMapLoader }) {
    sourceQueue.clear();
    sourceMapLoader.clearSourceMaps();
    if (features.codemirrorNext) {
      const editor = getEditor();
      editor.clearSources();
    }
    clearWasmStates();
    const thread = getMainThread(getState());

    dispatch({
      type: "NAVIGATE",
      mainThread: { ...thread, url: event.url },
    });
  };
}

/**
 * @memberof actions/navigation
 * @static
 */
export function navigated() {
  return async function ({ dispatch, panel }) {
    try {
      // Update the watched expressions once the page is fully loaded
      await dispatch(evaluateExpressionsForCurrentContext());
    } catch (e) {
      // This may throw if we resume during the page load.
      // browser_dbg-debugger-buttons.js highlights this, especially on MacOS or when ran many times
      console.error("Failed to update expression on navigation", e);
    }

    panel.emit("reloaded");
  };
}
