/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set ts=2 sts=2 et sw=2 tw=80: */
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef mozilla_dom_Formatters_h___
#define mozilla_dom_Formatters_h___

#include "fmt/base.h"

namespace mozilla::dom {
enum class NavigationType : uint8_t;
class NavigateEvent;
}  // namespace mozilla::dom

template <>
struct fmt::formatter<mozilla::dom::NavigationType> : formatter<string_view> {
  // parse is inherited from formatter<string_view>.

  auto format(mozilla::dom::NavigationType aNavigationType,
              format_context& ctx) const -> format_context::iterator;
};

template <>
struct fmt::formatter<mozilla::dom::NavigateEvent> : formatter<string_view> {
  // parse is inherited from formatter<string_view>.

  auto format(const mozilla::dom::NavigateEvent& aNavigateEvent,
              format_context& ctx) const -> format_context::iterator;
};

#endif  // mozilla_dom_Formatters_h___