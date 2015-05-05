/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Written by Jesper Smith with assistance from IHMC team members
 */
#ifndef JPEGENCODERIMPL_H
#define JPEGENCODERIMPL_H

#include "YUVPicture.h"
extern "C" {
	#include <jpeglib.h>
}

typedef struct {
		struct jpeg_destination_mgr destinationManager;

		unsigned char * outputBuffer;
		size_t bufferSize;
		size_t compressedSize;
} mem_destination_mgr;

class JPEGEncoderImpl
{
private:
	struct jpeg_compress_struct cinfo;
	struct jpeg_error_mgr jerr;
	mem_destination_mgr dest;


public:
	JPEGEncoderImpl();
	long long maxSize(YUVPicture* pic);
	int encode(YUVPicture* pic, uint8* target, int targetLength, int quality);
	~JPEGEncoderImpl();
};



#endif
