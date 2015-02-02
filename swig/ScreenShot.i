%module ScreenShot
%javaconst(1);
%include "typemaps.i"
%include "various.i"


%apply unsigned char *NIOBUFFER { unsigned char* };

%{

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <stdint.h>
#include <cstring>
#include <vector>

int getPixels(unsigned char* pixels, int x, int y, int width, int height)
{
	Display* display = XOpenDisplay(NULL);
	if(display == NULL)
	{
		return -1;
	}
	
	Window root = DefaultRootWindow(display);
	XWindowAttributes attributes = {0};
	XGetWindowAttributes(display, root, &attributes);

	XImage* img = XGetImage(display, root, x, y, width, height, AllPlanes, ZPixmap);

	if(img == NULL)
	{
		return -2;
	}
	int bitsPerPixel = img->bits_per_pixel;
	memcpy(pixels, img->data, width*height*(bitsPerPixel / 8));
	XDestroyImage(img);

	XCloseDisplay(display);
	return bitsPerPixel;
}

%}

int getPixels(unsigned char* pixels, int x, int y, int width, int height);

