%ignore Source_Picture_s::pData;
%ignore ISVCDecoder::DecodeFrame(const unsigned char*, const int, unsigned char**, int*, int&, int&);
%ignore ISVCDecoder::DecodeFrame2(const unsigned char*, const int, unsigned char**, SBufferInfo*);
%ignore ISVCDecoder::DecodeFrameEx(const unsigned char*, const int, unsigned char*, int, int&, int&, int&, int&);
%ignore ISVCDecoder::SetOption(DECODER_OPTION, void*);
%ignore ISVCDecoder::GetOption(DECODER_OPTION, void*);
%ignore ISVCEncoder::SetOption(ENCODER_OPTION, void*);
%ignore ISVCEncoder::GetOption(ENCODER_OPTION, void*);
