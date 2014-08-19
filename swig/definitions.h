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

        struct SBufferInfoExt {
                DECODING_STATE state;
                unsigned char* ppDst;
                SBufferInfo info;
        };
#ifdef SWIG
%mutable;
#endif
