package com.example.msme.exception;

public class PanNotFoundException extends RuntimeException {

  public PanNotFoundException(String pan) {
    super("PAN not found: " + pan);
  }
}
