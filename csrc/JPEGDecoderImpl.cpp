#include <JPEGDecoderImpl.h>
#include <iostream>

YUVPicture* JPEGDecoderImpl::decode(uint8* src, int srcLength) {
	LoadFrame(src, srcLength);
	int width = GetWidth();
	int height = GetHeight();
	int colorSpace = GetColorSpace();

	if (colorSpace == kColorSpaceUnknown) {
		std::cerr << "Unknown color space" << std::endl;
		UnloadFrame();
		return NULL;
	} else if (colorSpace == kColorSpaceGrayscale) {
		std::cerr << "Cannot handle gray scale" << std::endl;
		UnloadFrame();
		return NULL;
	} else if (colorSpace == kColorSpaceRgb) {
		std::cerr << "Cannot handle RGB color space" << std::endl;
		UnloadFrame();
		return NULL;
	} else if (colorSpace == kColorSpaceCMYK) {
		std::cerr << "Cannot handle CMYK color space" << std::endl;
		UnloadFrame();
		return NULL;
	} else if (colorSpace == kColorSpaceYCCK) {
		std::cerr << "Cannot handle YCCK color space" << std::endl;
		UnloadFrame();
		return NULL;
	}

	int numberOfPlanes = GetNumComponents();

	if (numberOfPlanes != 3) {
		std::cerr << "Can only handle YUV color MJPEG tracks with 3 components" << std::endl;
		UnloadFrame();
		return NULL;
	}

	YUVPicture::YUVSubsamplingType samplingType = YUVPicture::getSubsamplingType(GetComponentWidth(0), GetComponentHeight(0), GetComponentWidth(1), GetComponentHeight(1), GetComponentWidth(2), GetComponentHeight(2));
	if(samplingType == YUVPicture::UNSUPPORTED)
	{
		std::cerr << "Unknown sampling type" << std::endl;
		UnloadFrame();
		return NULL;
	}

	YUVPicture* picture = new YUVPicture(samplingType, width, height);

	uint8* planes[] = { picture->getY(), picture->getU(), picture->getV() };
	DecodeToBuffers(planes, width, height);
	UnloadFrame();

	return picture;
}
