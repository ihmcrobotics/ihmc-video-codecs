%extend ISVCDecoder {

	%newobject DecodeFrame;
	STargetPicture* DecodeFrame(const unsigned char* pSrc, const int iSrcLen)
	{
		STargetPicture* ret = new STargetPicture;
		ret->state = $self->DecodeFrame(pSrc, iSrcLen, &ret->ppDst, ret->pStride, ret->iWidth, ret->iHeight);
		ret->iColorFormat = videoFormatI420;
		return ret;
	}

	%newobject DecodeFrame2;
	SBufferInfoExt* DecodeFrame2(const unsigned char* pSrc, const int iSrcLen)
	{
		SBufferInfoExt* ret = new SBufferInfoExt;
		ret->state = $self->DecodeFrame2(pSrc, iSrcLen, &ret->ppDst, &ret->info);
		return ret;
	}
	long SetOption(DECODER_OPTION eOptionId, int option)		
	{
		return $self->SetOption(eOptionId, &option);
	}

	long SetOption(DECODER_OPTION eOptionId, bool option)
	{
		return $self->SetOption(eOptionId, &option);
	}

	int GetIntOption(DECODER_OPTION eOptionId)
	{
	    int ret;
	    $self->GetOption(eOptionId, &ret);
	    return ret;
	}

	bool GetBoolOption(DECODER_OPTION eOptionId)
	{
	    bool ret;
	    $self->GetOption(eOptionId, &ret);
	    return ret;
	}
}

