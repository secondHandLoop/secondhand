package com.secondhand.common.exception;



@SuppressWarnings("unused")
public class JSONException extends RuntimeException {

  private int code;

  public JSONException(int code) {
    this.code = code;
  }

  public JSONException(String message, int code) {
    super(message);
    this.code = code;
  }

  public JSONException(String message, Throwable cause, int code) {
    super(message, cause);
    this.code = code;
  }

  public JSONException(Throwable cause, int code) {
    super(cause);
    this.code = code;
  }

  public JSONException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int code) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

}
