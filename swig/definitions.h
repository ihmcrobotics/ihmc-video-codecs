#ifdef SWIG
%immutable;
#endif
        struct STargetPicture {
                DECODING_STATE state;
                unsigned char* ppDst;
                int pStride[2];
                int iWidth;
                int iHeight;
                int iColorFormat;
        };

        class SBufferInfoExt {
	public:
                DECODING_STATE state;
                unsigned char* ppDst;
                SBufferInfo info;
		
		void getPpDst(unsigned char* ppDst)
		{
			SSysMEMBuffer* buf = (SSysMEMBuffer*) &this->info.UsrData;
			int size = buf->iStride[0] * buf->iHeight + 2 * buf->iStride[1] * buf->iHeight;
			memcpy(ppDst, this->ppDst, size); 
		}

        };
#ifdef SWIG
%mutable;
#endif
