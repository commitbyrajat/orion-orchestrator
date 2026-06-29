package com.example.msme.util;

import com.example.msme.exception.InvalidPanException;
import java.util.Locale;

public final class PanValidator {

  public static final String PAN_REGEX = "[A-Z]{5}[0-9]{4}[A-Z]";

  private PanValidator() {
  }

  public static String normalizeAndValidate(String pan) {
    String normalized = pan == null ? "" : pan.trim().toUpperCase(Locale.ROOT);
    if (!normalized.matches(PAN_REGEX)) {
      throw new InvalidPanException("PAN must match " + PAN_REGEX);
    }
    return normalized;
  }
}
