#include <YUVPicture.h>
#include <stdlib.h>
#include <RGBPicture.h>
#include <string.h>

bool YUVPicture::isHalf(int orig, int toTest) {
	if (orig / 2 == toTest) {
		return true;
	} else if (orig / 2 == toTest + 1) {
		return true;
	} else if (orig / 2 == toTest - 1) {
		return true;
	}

	return false;
}

inline int YUVPicture::divideByTwoRoundUp(int orig) {
	return (orig + 1) >> 1;
}
YUVPicture::YUVSubsamplingType YUVPicture::getSubsamplingType(int yWidth, int yHeight, int uWidth, int uHeight, int vWidth, int vHeight) {
	if (yWidth == uWidth && yWidth == vWidth && yHeight == uHeight && yHeight == vHeight) {
		return YUVPicture::YUV444;
	} else if (YUVPicture::isHalf(yWidth, uWidth) && YUVPicture::isHalf(yWidth, vWidth) && yHeight == uHeight && yHeight == vHeight) {
		return YUVPicture::YUV422;
	} else if (YUVPicture::isHalf(yWidth, uWidth) && YUVPicture::isHalf(yWidth, vWidth) && YUVPicture::isHalf(yHeight, uHeight) && YUVPicture::isHalf(yHeight, vHeight)) {
		return YUVPicture::YUV420;
	} else
		return YUVPicture::UNSUPPORTED;
}
YUVPicture::YUVPicture(YUVSubsamplingType type, int width, int height, int yStride, int uStride, int vStride, uint8 *Yin, uint8 *Uin, uint8 *Vin) :
		type(type), width(width), height(height), yStride(yStride), uStride(uStride), vStride(vStride) {
	int ySize = yStride * height;
	int uSize = uStride * YUVPicture::divideByTwoRoundUp(height);
	int vSize = vStride * YUVPicture::divideByTwoRoundUp(height);

	Y = (uint8*) malloc(ySize);
	U = (uint8*) malloc(uSize);
	V = (uint8*) malloc(vSize);

	memcpy(Y, Yin, ySize);
	memcpy(U, Uin, uSize);
	memcpy(V, Vin, vSize);

}
YUVPicture::YUVPicture(YUVSubsamplingType type, int width, int height) :
		type(type), width(width), height(height) {
	switch (type) {
	case YUV420: {
		yStride = width;
		uStride = YUVPicture::divideByTwoRoundUp(width);
		vStride = YUVPicture::divideByTwoRoundUp(width);

		Y = (uint8*) malloc(yStride * height);
		U = (uint8*) malloc(uStride * YUVPicture::divideByTwoRoundUp(height));
		V = (uint8*) malloc(vStride * YUVPicture::divideByTwoRoundUp(height));
		break;
	}
	case YUV422: {
		yStride = width;
		uStride = YUVPicture::divideByTwoRoundUp(width);
		vStride = YUVPicture::divideByTwoRoundUp(width);

		Y = (uint8*) malloc(yStride * height);
		U = (uint8*) malloc(uStride * height);
		V = (uint8*) malloc(vStride * height);
		break;
	}
	case YUV444: {
		yStride = width;
		uStride = width;
		vStride = width;

		Y = (uint8*) malloc(yStride * height);
		U = (uint8*) malloc(uStride * height);
		V = (uint8*) malloc(vStride * height);
		break;
	}
	}
}

void YUVPicture::scale(int newWidth, int newHeight, libyuv::FilterModeEnum filterMode) {
	uint8 *Ydest, *Udest, *Vdest;
	int yStrideDest, uStrideDest, vStrideDest;

	switch (type) {
	case YUV420: {
		yStrideDest = newWidth;
		uStrideDest = divideByTwoRoundUp(yStrideDest);
		vStrideDest = divideByTwoRoundUp(yStrideDest);

		Ydest = (uint8*) malloc(yStrideDest * newHeight);
		Udest = (uint8*) malloc(uStrideDest * (newHeight >> 1));
		Vdest = (uint8*) malloc(vStrideDest * (newHeight >> 1));
		libyuv::I420Scale(Y, yStride, U, uStride, V, vStride, width, height, Ydest, yStrideDest, Udest, uStrideDest, Vdest, vStrideDest, newWidth, newHeight, filterMode);
		break;
	}
	case YUV422: {
		yStrideDest = newWidth;
		uStrideDest = divideByTwoRoundUp(yStrideDest);
		vStrideDest = divideByTwoRoundUp(yStrideDest);

		Ydest = (uint8*) malloc(yStrideDest * newHeight);
		Udest = (uint8*) malloc(uStrideDest * newHeight);
		Vdest = (uint8*) malloc(vStrideDest * newHeight);

		libyuv::ScalePlane(Y, yStride, width, height, Ydest, yStrideDest, newWidth, newHeight, filterMode);
		libyuv::ScalePlane(U, uStride, width, height, Udest, uStrideDest, newWidth / 2, newHeight, filterMode);
		libyuv::ScalePlane(V, vStride, width, height, Vdest, vStrideDest, newWidth / 2, newHeight, filterMode);
		break;
	}
	case YUV444: {
		yStrideDest = newWidth;
		uStrideDest = yStrideDest;
		vStrideDest = yStrideDest;

		Ydest = (uint8*) malloc(yStrideDest * newHeight);
		Udest = (uint8*) malloc(uStrideDest * newHeight);
		Vdest = (uint8*) malloc(vStrideDest * newHeight);

		libyuv::ScalePlane(Y, yStride, width, height, Ydest, yStrideDest, newWidth, newHeight, filterMode);
		libyuv::ScalePlane(U, uStride, width, height, Udest, uStrideDest, newWidth, newHeight, filterMode);
		libyuv::ScalePlane(V, vStride, width, height, Vdest, vStrideDest, newWidth, newHeight, filterMode);

		break;
	}
	}

	yStride = yStrideDest;
	uStride = uStrideDest;
	vStride = vStrideDest;

	width = newWidth;
	height = newHeight;

	free(Y);
	free(U);
	free(V);

	Y = Ydest;
	U = Udest;
	V = Vdest;
}

RGBPicture* YUVPicture::toRGB() {
	RGBPicture *rgb = new RGBPicture(width, height);
	uint8* dstBuffer = rgb->getBuffer();
	unsigned int dstStride = width * 3;

	switch (type) {
	case YUV420: {
		libyuv::I420ToRGB24(Y, yStride, U, uStride, V, vStride, dstBuffer, dstStride, width, height);
		break;
	}
	case YUV422: {
		unsigned int tmpStride = width * 4;
		uint8* tmpBuffer = (uint8*) malloc(width * height * 4);
		libyuv::I422ToARGB(Y, yStride, U, uStride, V, vStride, tmpBuffer, tmpStride, width, height);
		libyuv::ARGBToRGB24(tmpBuffer, tmpStride, dstBuffer, dstStride, width, height);
		free(tmpBuffer);
		break;
	}
	case YUV444: {
		unsigned int tmpStride = width * 4;
		uint8* tmpBuffer = (uint8*) malloc(width * height * 4);
		libyuv::I444ToARGB(Y, yStride, U, uStride, V, vStride, tmpBuffer, tmpStride, width, height);
		libyuv::ARGBToRGB24(tmpBuffer, tmpStride, dstBuffer, dstStride, width, height);
		free(tmpBuffer);
		break;
	}
	}

	return rgb;
}

void YUVPicture::toYUV420() {
	if (type == YUV420) {
		return;
	}
	int yStrideDest, uStrideDest, vStrideDest;
	uint8 *Ydest, *Udest, *Vdest;

	if (type == YUV422) {

		yStrideDest = width;
		uStrideDest = divideByTwoRoundUp(yStrideDest);
		vStrideDest = divideByTwoRoundUp(yStrideDest);

		Ydest = (uint8*) malloc(yStrideDest * height);
		Udest = (uint8*) malloc(uStrideDest * height);
		Vdest = (uint8*) malloc(vStrideDest * height);

		libyuv::I422ToI420(Y, yStride, U, uStride, V, vStride, Ydest, yStrideDest, Udest, uStrideDest, Vdest, vStrideDest, width, height);

	} else if (type == YUV444) {
		yStrideDest = width;
		uStrideDest = yStrideDest;
		vStrideDest = yStrideDest;

		Ydest = (uint8*) malloc(yStrideDest * height);
		Udest = (uint8*) malloc(uStrideDest * height);
		Vdest = (uint8*) malloc(vStrideDest * height);

		libyuv::I444ToI420(Y, yStride, U, uStride, V, vStride, Ydest, yStrideDest, Udest, uStrideDest, Vdest, vStrideDest, width, height);
	}

	yStride = yStrideDest;
	uStride = uStrideDest;
	vStride = vStrideDest;

	free(Y);
	free(U);
	free(V);

	Y = Ydest;
	U = Udest;
	V = Vdest;
}

YUVPicture::YUVSubsamplingType YUVPicture::getType() {
	return type;
}

uint8* YUVPicture::getY() {
	return Y;
}
uint8* YUVPicture::getU() {
	return U;
}
uint8* YUVPicture::getV() {
	return V;
}

int YUVPicture::getWidth() {
	return width;
}
int YUVPicture::getHeight() {
	return height;
}
int YUVPicture::getYStride() {
	return yStride;
}
int YUVPicture::getUStride() {
	return uStride;
}
int YUVPicture::getVStride() {
	return vStride;
}

YUVPicture::~YUVPicture() {
	free(Y);
	free(U);
	free(V);
}
