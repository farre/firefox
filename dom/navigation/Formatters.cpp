/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set ts=2 sts=2 et sw=2 tw=80: */
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "Formatters.h"

#include "mozilla/dom/NavigationBinding.h"
#include "mozilla/dom/NavigateEvent.h"
#include "mozilla/dom/UserNavigationInvolvement.h"

#include "fmt/format.h"

enum class NavigationType : uint8_t {
  Push,
  Replace,
  Reload,
  Traverse,
};

auto fmt::formatter<mozilla::dom::NavigationType>::format(
    mozilla::dom::NavigationType aNavigationType, format_context& ctx) const
    -> format_context::iterator {
  using NavigationType = mozilla::dom::NavigationType;
  string_view name = "unknown";
  switch (aNavigationType) {
    case NavigationType::Push:
      name = "push";
      break;
    case NavigationType::Replace:
      name = "replace";
      break;
    case NavigationType::Reload:
      name = "reload";
      break;
    case NavigationType::Traverse:
      name = "traverse";
      break;
  }
  return formatter<string_view>::format(name, ctx);
}

auto fmt::formatter<mozilla::dom::NavigateEvent>::format(
    const mozilla::dom::NavigateEvent& aNavigateEvent,
    format_context& ctx) const -> format_context::iterator {
  return formatter<string_view>::format(
      fmt::format(FMT_STRING("NavigateEvent type={}"),
                  aNavigateEvent.NavigationType()),
      ctx);
}