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
 *
 * This file includes code from libjpeg-turbo. License reproduced below.
 *---------------------------------------------------------------------------
 *
 * Copyright (C)2009-2014 D. R. Commander. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of the libjpeg-turbo Project nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS",
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
#include <stdio.h>
#include "JPEGEncoderImpl.h"
#include <string.h>
#include <iostream>
#include <algorithm>

#define PAD(v, p) ((v+(p)-1)&(~((p)-1)))



// Destination manager as old-school C code
void memory_init_destination(j_compress_ptr cinfo) {

	mem_destination_mgr* dest = (mem_destination_mgr*) cinfo->dest;

	dest->destinationManager.next_output_byte = dest->outputBuffer;
	dest->destinationManager.free_in_buffer = dest->bufferSize;
}

boolean memory_empty_output_buffer(j_compress_ptr cinfo) {
	return false;
}

void memory_term_destination(j_compress_ptr cinfo) {
	mem_destination_mgr* dest = (mem_destination_mgr*) cinfo->dest;

	dest->compressedSize = dest->bufferSize - dest->destinationManager.free_in_buffer;
}

JPEGEncoderImpl::JPEGEncoderImpl() {
	cinfo.err = jpeg_std_error(&jerr);
	jpeg_create_compress(&cinfo);

	dest.destinationManager.init_destination = &memory_init_destination;
	dest.destinationManager.empty_output_buffer = &memory_empty_output_buffer;
	dest.destinationManager.term_destination = &memory_term_destination;

	cinfo.dest = (struct jpeg_destination_mgr *) &dest;
}

// Based on turbojpeg.c:tjBufSize()
long long JPEGEncoderImpl::maxSize(YUVPicture* pic) {
	int mcuw, mcuh;
	/* This allows for rare corner cases in which a JPEG image can actually be
	 larger than the uncompressed input (we wouldn't mention it if it hadn't
	 happened before.) */
	switch (pic->getType()) {
	case YUVPicture::YUV420:
		mcuw = 16;
		mcuh = 16;
		break;
	case YUVPicture::YUV422:
		mcuw = 16;
		mcuh = 8;
		break;
	case YUVPicture::YUV444:
		mcuw = 8;
		mcuh = 8;
		break;
	}
	int chromasf = 4 * 64 / (mcuw * mcuh);
	return PAD(pic->getWidth(), mcuw) * PAD(pic->getHeight(), mcuh) * (2 + chromasf) + 2048;
}

int JPEGEncoderImpl::encode(YUVPicture* pic, unsigned char* target, int targetLength, int quality) {

	/*
	 * Setup destination manager.
	 */
	dest.compressedSize = -1; // Default to error value
	dest.outputBuffer = target;
	dest.bufferSize = targetLength;

	/*
	 * Setup cinfo
	 */
	cinfo.image_width = pic->getWidth();
	cinfo.image_height = pic->getHeight();
	cinfo.input_components = 3;
	cinfo.in_color_space = JCS_RGB;
	jpeg_set_defaults(&cinfo);
	jpeg_set_colorspace(&cinfo, JCS_YCbCr);
	jpeg_set_quality(&cinfo, quality, TRUE);
	cinfo.raw_data_in = TRUE;
	switch (pic->getType()) {
	case YUVPicture::YUV420:
		cinfo.comp_info[0].h_samp_factor = 2;
		cinfo.comp_info[0].v_samp_factor = 2;
		cinfo.comp_info[1].h_samp_factor = 1;
		cinfo.comp_info[1].v_samp_factor = 1;
		cinfo.comp_info[2].h_samp_factor = 1;
		cinfo.comp_info[2].v_samp_factor = 1;
		break;
	case YUVPicture::YUV422:
		cinfo.comp_info[0].h_samp_factor = 2;
		cinfo.comp_info[0].v_samp_factor = 1;
		cinfo.comp_info[1].h_samp_factor = 1;
		cinfo.comp_info[1].v_samp_factor = 1;
		cinfo.comp_info[2].h_samp_factor = 1;
		cinfo.comp_info[2].v_samp_factor = 1;
		break;
	case YUVPicture::YUV444:
		cinfo.comp_info[0].h_samp_factor = 1;
		cinfo.comp_info[0].v_samp_factor = 1;
		cinfo.comp_info[1].h_samp_factor = 1;
		cinfo.comp_info[1].v_samp_factor = 1;
		cinfo.comp_info[2].h_samp_factor = 1;
		cinfo.comp_info[2].v_samp_factor = 1;
		break;
	}

	/*
	 * Start compression
	 */
	jpeg_start_compress(&cinfo, TRUE);
	// The following routine is based on turbojpeg.c:tjCompressFromYUVPlanes()
	uint8 *srcPlanes[] = { pic->getY(), pic->getU(), pic->getV() };
	int strides[] = { pic->getYStride(), pic->getUStride(), pic->getVStride() };
	int pw[MAX_COMPONENTS], ph[MAX_COMPONENTS], iw[MAX_COMPONENTS], tmpbufsize = 0, usetmpbuf = 0, th[MAX_COMPONENTS];
	JSAMPLE *_tmpbuf = NULL, *ptr;
	JSAMPROW *inbuf[MAX_COMPONENTS];
	JSAMPROW *tmpbuf[MAX_COMPONENTS];

	for (int i = 0; i < MAX_COMPONENTS; i++) {
		tmpbuf[i] = NULL;
		inbuf[i] = NULL;
	}

	for (int i = 0; i < cinfo.num_components; i++) {
		jpeg_component_info *compptr = &cinfo.comp_info[i];
		int ih;
		iw[i] = compptr->width_in_blocks * DCTSIZE;
		ih = compptr->height_in_blocks * DCTSIZE;
		pw[i] = PAD(cinfo.image_width, cinfo.max_h_samp_factor) * compptr->h_samp_factor / cinfo.max_h_samp_factor;

		// To support images with uneven height, take the min of calculated plane height and image height.
		ph[i] = std::min(cinfo.image_height, (PAD(cinfo.image_height, cinfo.max_v_samp_factor) * compptr->v_samp_factor / cinfo.max_v_samp_factor));
		if (iw[i] != pw[i] || ih != ph[i])
			usetmpbuf = 1;
		th[i] = compptr->v_samp_factor * DCTSIZE;
		tmpbufsize += iw[i] * th[i];
		if ((inbuf[i] = (JSAMPROW *) malloc(sizeof(JSAMPROW) * ph[i])) == NULL) {
			std::cerr << "JPEGEncoderImpl::encoder: Memory allocation failure" << std::endl;
			return -1;
		}
		ptr = srcPlanes[i];
		for (int row = 0; row < ph[i]; row++) {
			inbuf[i][row] = ptr;
			ptr += (strides && strides[i] != 0) ? strides[i] : pw[i];
		}
	}
	if (usetmpbuf) {
		if ((_tmpbuf = (JSAMPLE *) malloc(sizeof(JSAMPLE) * tmpbufsize)) == NULL) {
			std::cerr << "JPEGEncoderImpl::encoder: Memory allocation failure" << std::endl;
			return -1;
		}
		ptr = _tmpbuf;
		for (int i = 0; i < cinfo.num_components; i++) {
			if ((tmpbuf[i] = (JSAMPROW *) malloc(sizeof(JSAMPROW) * th[i])) == NULL) {
				std::cerr << "JPEGEncoderImpl::encoder: Memory allocation failure" << std::endl;
			}
			for (int row = 0; row < th[i]; row++) {
				tmpbuf[i][row] = ptr;
				ptr += iw[i];
			}
		}
	}

	for (int row = 0; row < (int) cinfo.image_height; row += cinfo.max_v_samp_factor * DCTSIZE) {
		JSAMPARRAY yuvptr[MAX_COMPONENTS];
		int crow[MAX_COMPONENTS];
		for (int i = 0; i < cinfo.num_components; i++) {
			jpeg_component_info *compptr = &cinfo.comp_info[i];
			crow[i] = row * compptr->v_samp_factor / cinfo.max_v_samp_factor;
			if (usetmpbuf) {
				int j, k;
				for (j = 0; j < std::min(th[i], ph[i] - crow[i]); j++) {
					memcpy(tmpbuf[i][j], inbuf[i][crow[i] + j], pw[i]);
					/* Duplicate last sample in row to fill out MCU */

					for (k = pw[i]; k < iw[i]; k++)
						tmpbuf[i][j][k] = tmpbuf[i][j][pw[i] - 1];
				}
				/* Duplicate last row to fill out MCU */
				for (j = ph[i] - crow[i]; j < th[i]; j++)
					memcpy(tmpbuf[i][j], tmpbuf[i][ph[i] - crow[i] - 1], iw[i]);

				yuvptr[i] = tmpbuf[i];
			} else
				yuvptr[i] = &inbuf[i][crow[i]];
		}
		jpeg_write_raw_data(&cinfo, yuvptr, cinfo.max_v_samp_factor * DCTSIZE);
	}

	jpeg_finish_compress(&cinfo);

	for (int i = 0; i < MAX_COMPONENTS; i++) {
		if (tmpbuf[i]) {
			free(tmpbuf[i]);
		}
		if (inbuf[i]) {
			free(inbuf[i]);
		}
	}
	if (_tmpbuf)
		free(_tmpbuf);

	/*
	 * Return compressedSize as set by the destination manager
	 */
	return dest.compressedSize;
}

JPEGEncoderImpl::~JPEGEncoderImpl() {
	jpeg_destroy_compress(&cinfo);
}
