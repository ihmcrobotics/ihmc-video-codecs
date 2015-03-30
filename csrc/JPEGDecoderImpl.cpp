#include <JPEGDecoderImpl.h>
#include <iostream>

YUVPicture* JPEGDecoderImpl::decode(uint8* src, int srcLength) {
	if(!decoder.LoadFrame(src, srcLength))
	{
		return NULL;
	}
	int width = decoder.GetWidth();
	int height = decoder.GetHeight();
	int colorSpace = decoder.GetColorSpace();

	if (colorSpace == decoder.kColorSpaceUnknown) {
		std::cerr << "Unknown color space" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	} else if (colorSpace == decoder.kColorSpaceGrayscale) {
		std::cerr << "Cannot handle gray scale" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	} else if (colorSpace == decoder.kColorSpaceRgb) {
		std::cerr << "Cannot handle RGB color space" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	} else if (colorSpace == decoder.kColorSpaceCMYK) {
		std::cerr << "Cannot handle CMYK color space" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	} else if (colorSpace == decoder.kColorSpaceYCCK) {
		std::cerr << "Cannot handle YCCK color space" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	}

	int numberOfPlanes = decoder.GetNumComponents();

	if (numberOfPlanes != 3) {
		std::cerr << "Can only handle YUV color MJPEG tracks with 3 components" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	}

	YUVPicture::YUVSubsamplingType samplingType = YUVPicture::getSubsamplingType(decoder.GetComponentWidth(0), decoder.GetComponentHeight(0), decoder.GetComponentWidth(1), decoder.GetComponentHeight(1), decoder.GetComponentWidth(2), decoder.GetComponentHeight(2));
	if(samplingType == YUVPicture::UNSUPPORTED)
	{
		std::cerr << "Unknown sampling type" << std::endl;
		decoder.UnloadFrame();
		return NULL;
	}
	YUVPicture* picture = new YUVPicture(samplingType, width, height);

	uint8* planes[] = { picture->getY(), picture->getU(), picture->getV() };
	decoder.DecodeToBuffers(planes, width, height);
	decoder.UnloadFrame();

	return picture;
}
