/* -*- Mode: C++; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set ts=8 sts=2 et sw=2 tw=80: */
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef mozilla_dom_textdecoder_h_
#define mozilla_dom_textdecoder_h_

#include "mozilla/dom/BufferSourceBindingFwd.h"
#include "mozilla/dom/NonRefcountedDOMObject.h"
#include "mozilla/dom/TextDecoderBinding.h"
#include "mozilla/dom/TypedArray.h"
#include "mozilla/Encoding.h"
#include "mozilla/ErrorResult.h"
#include "mozilla/UniquePtr.h"

namespace mozilla::dom {

class TextDecoderCommon {
 public:
  /**
   * Decodes incoming byte stream of characters in charset indicated by
   * encoding.
   *
   * The encoding algorithm state is reset if aOptions.mStream is not set.
   *
   * If the fatal flag is set then a decoding error will throw EncodingError.
   * Else the decoder will return a decoded string with replacement
   * character(s) for unidentified character(s).
   *
   * @param      aInput, incoming byte stream of characters to be decoded to
   *                    to UTF-16 code points.
   * @param      aStream, indicates if streaming or not.
   * @param      aOutDecodedString, decoded string of UTF-16 code points.
   * @param      aRv, error result.
   */
  void DecodeNative(mozilla::Span<const uint8_t> aInput, const bool aStream,
                    nsAString& aOutDecodedString, ErrorResult& aRv);

  /**
   * Return the encoding name.
   *
   * @param aEncoding, current encoding.
   */
  void GetEncoding(nsAString& aEncoding);

  bool Fatal() const { return mFatal; }

  bool IgnoreBOM() const { return mIgnoreBOM; }

 protected:
  mozilla::UniquePtr<mozilla::Decoder> mDecoder;
  nsCString mEncoding;
  bool mFatal = false;
  bool mIgnoreBOM = false;
};

class TextDecoder final : public NonRefcountedDOMObject,
                          public TextDecoderCommon {
 public:
  // The WebIDL constructor.
  static UniquePtr<TextDecoder> Constructor(const GlobalObject& aGlobal,
                                            const nsAString& aEncoding,
                                            const TextDecoderOptions& aOptions,
                                            ErrorResult& aRv) {
    auto txtDecoder = MakeUnique<TextDecoder>();
    txtDecoder->Init(aEncoding, aOptions, aRv);
    if (aRv.Failed()) {
      return nullptr;
    }
    return txtDecoder;
  }

  TextDecoder() { MOZ_COUNT_CTOR(TextDecoder); }

  MOZ_COUNTED_DTOR(TextDecoder)

  bool WrapObject(JSContext* aCx, JS::Handle<JSObject*> aGivenProto,
                  JS::MutableHandle<JSObject*> aReflector) {
    return TextDecoder_Binding::Wrap(aCx, this, aGivenProto, aReflector);
  }

  /**
   * Validates provided label and throws an exception if invalid label.
   *
   * @param aLabel       The encoding label (case insensitive) provided.
   * @param aOptions     The TextDecoderOptions to use.
   * @return aRv         EncodingError exception else null.
   */
  void Init(const nsAString& aLabel, const TextDecoderOptions& aOptions,
            ErrorResult& aRv);

  /**
   * Performs initialization with a Gecko-canonical encoding name (as opposed
   * to a label.)
   *
   * @param aEncoding    An Encoding object
   * @param aOptions     The TextDecoderOptions to use.
   */
  void InitWithEncoding(NotNull<const Encoding*> aEncoding,
                        const TextDecoderOptions& aOptions);

  void Decode(const Optional<BufferSource>& aBuffer,
              const TextDecodeOptions& aOptions, nsAString& aOutDecodedString,
              ErrorResult& aRv);

 private:
};

}  // namespace mozilla::dom

#endif  // mozilla_dom_textdecoder_h_
