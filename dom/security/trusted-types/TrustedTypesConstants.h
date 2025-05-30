/* -*- Mode: C++; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set ts=8 sts=2 et sw=2 tw=80: */
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef DOM_SECURITY_TRUSTED_TYPES_TRUSTEDTYPESCONSTANTS_H_
#define DOM_SECURITY_TRUSTED_TYPES_TRUSTEDTYPESCONSTANTS_H_

#include "nsLiteralString.h"

namespace mozilla::dom {
// https://w3c.github.io/trusted-types/dist/spec/#integration-with-content-security-policy
static constexpr nsLiteralString kValidRequireTrustedTypesForDirectiveValue =
    u"'script'"_ns;

// https://github.com/w3c/trusted-types/issues/542
constexpr nsLiteralString kTrustedTypesOnlySinkGroup =
    kValidRequireTrustedTypesForDirectiveValue;

}  // namespace mozilla::dom

#endif  // DOM_SECURITY_TRUSTED_TYPES_TRUSTEDTYPESCONSTANTS_H_
