/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package us.ihmc.codecs.generated;

public class OpenH264EncoderImpl {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OpenH264EncoderImpl(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OpenH264EncoderImpl obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        ihmcVideoCodecsJNI.delete_OpenH264EncoderImpl(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OpenH264EncoderImpl() {
    this(ihmcVideoCodecsJNI.new_OpenH264EncoderImpl(), true);
  }

  public void setUsageType(EUsageType usage) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setUsageType(swigCPtr, this, usage.swigValue());
  }

  public void setSize(int width, int height) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setSize(swigCPtr, this, width, height);
  }

  public void setBitRate(int bitrate) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setBitRate(swigCPtr, this, bitrate);
  }

  public void setRCMode(RC_MODES mode) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setRCMode(swigCPtr, this, mode.swigValue());
  }

  public void setMaxFrameRate(float rate) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setMaxFrameRate(swigCPtr, this, rate);
  }

  public void setComplexityMode(ECOMPLEXITY_MODE mode) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setComplexityMode(swigCPtr, this, mode.swigValue());
  }

  public void setIntraPeriod(int period) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setIntraPeriod(swigCPtr, this, period);
  }

  public void setEnableSpsPpsIdAddition(boolean enable) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setEnableSpsPpsIdAddition(swigCPtr, this, enable);
  }

  public void setEnableFrameSkip(boolean enable) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setEnableFrameSkip(swigCPtr, this, enable);
  }

  public void setMaxBitrate(int maxBitrate) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setMaxBitrate(swigCPtr, this, maxBitrate);
  }

  public void setMaxQp(int maxQp) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setMaxQp(swigCPtr, this, maxQp);
  }

  public void setMinQp(int minQp) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setMinQp(swigCPtr, this, minQp);
  }

  public void setEnableDenoise(boolean enable) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setEnableDenoise(swigCPtr, this, enable);
  }

  public boolean initialize() {
    return ihmcVideoCodecsJNI.OpenH264EncoderImpl_initialize(swigCPtr, this);
  }

  public boolean encodeFrameImpl(YUVPicture frame) {
    return ihmcVideoCodecsJNI.OpenH264EncoderImpl_encodeFrameImpl(swigCPtr, this, YUVPicture.getCPtr(frame), frame);
  }

  public boolean nextNAL() {
    return ihmcVideoCodecsJNI.OpenH264EncoderImpl_nextNAL(swigCPtr, this);
  }

  public int getNALSize() {
    return ihmcVideoCodecsJNI.OpenH264EncoderImpl_getNALSize(swigCPtr, this);
  }

  public void getNAL(java.nio.ByteBuffer buffer, int bufferSize) {
  assert buffer.isDirect() : "Buffer must be allocated direct.";
    {
      ihmcVideoCodecsJNI.OpenH264EncoderImpl_getNAL(swigCPtr, this, buffer, bufferSize);
    }
  }

  public void sendIntraFrame() {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_sendIntraFrame(swigCPtr, this);
  }

  public void setLevelIDC(ELevelIdc level) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setLevelIDC(swigCPtr, this, level.swigValue());
  }

  public void setProfileIdc(EProfileIdc profile) {
    ihmcVideoCodecsJNI.OpenH264EncoderImpl_setProfileIdc(swigCPtr, this, profile.swigValue());
  }

}
