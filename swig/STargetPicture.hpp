#ifdef SWIG
%immutable;
%ignore STargetPicture::ppDst;
#endif
        class STargetPicture {
	public:
                unsigned char* ppDst[3];
		SBufferInfo info;
		STargetPicture()
		{
			ppDst[0] = NULL;
			ppDst[1] = NULL;
			ppDst[2] = NULL;
		}
	
		void getY(unsigned char* Y)
		{
			SSysMEMBuffer* buf = (SSysMEMBuffer*) &info.UsrData;
			int size = buf->iStride[0] * buf->iHeight;
			memcpy(Y, this->ppDst[0], size); 
		}
		void getU(unsigned char* U)
		{
			SSysMEMBuffer* buf = (SSysMEMBuffer*) &info.UsrData;
			int size = buf->iStride[1] * (buf->iHeight >> 1);
			memcpy(U, this->ppDst[1], size); 
		}
		void getV(unsigned char* V)
		{
			SSysMEMBuffer* buf = (SSysMEMBuffer*) &info.UsrData;
			int size = buf->iStride[1] * (buf->iHeight >> 1);
			memcpy(V, this->ppDst[2], size); 
		}

        };
#ifdef SWIG
%mutable;
#endif
