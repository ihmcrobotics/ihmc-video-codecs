#include <RGBPicture.h>
#include <stdlib.h>
#include <string.h>
RGBPicture::RGBPicture(int width, int height) :
		width(width), height(height) {
	buffer = (uint8*) malloc(width * height * 3);
}

void RGBPicture::put(uint8* src) {
	memcpy(buffer, src, width * height * 3);
}

void RGBPicture::putRGBA(uint8* src) {
	libyuv::ARGBToRGB24(src, width * 4, buffer, width * 3, width, height);
}

void RGBPicture::get(uint8* target) {
	memcpy(target, buffer, width * height * 3);
}

uint8* RGBPicture::getBuffer() {
	return buffer;
}

int RGBPicture::getWidth() {
	return width;
}
int RGBPicture::getHeight() {
	return height;
}

YUVPicture* RGBPicture::toYUV(YUVPicture::YUVSubsamplingType samplingType) {
	YUVPicture* target = new YUVPicture(samplingType, getWidth(), getHeight());

	int srcStride = getWidth() * 3;
	switch (samplingType) {
	case YUVPicture::YUV444: {
		int dstStride = getWidth() * 4;
		uint8* bgra = (uint8*) malloc(dstStride * getHeight());
		libyuv::RGB24ToARGB(buffer, srcStride, bgra, dstStride, getWidth(), getHeight());
		libyuv::ARGBToI444(bgra, dstStride, target->getY(), target->getYStride(), target->getU(), target->getUStride(), target->getV(), target->getVStride(), getWidth(), getHeight());
		break;
	}
	case YUVPicture::YUV422: {
		int dstStride = getWidth() * 4;
		uint8* bgra = (uint8*) malloc(dstStride * getHeight());
		libyuv::RGB24ToARGB(buffer, srcStride, bgra, dstStride, getWidth(), getHeight());
		libyuv::ARGBToI422(bgra, dstStride, target->getY(), target->getYStride(), target->getU(), target->getUStride(), target->getV(), target->getVStride(), getWidth(), getHeight());
		break;
	}
	case YUVPicture::YUV420: {
		libyuv::RGB24ToI420(buffer, srcStride, target->getY(), target->getYStride(), target->getU(), target->getUStride(), target->getV(), target->getVStride(), getWidth(), getHeight());
		break;
	}
	default:
		break;
	}

	return target;
}

RGBPicture::~RGBPicture() {
	free(buffer);
}
