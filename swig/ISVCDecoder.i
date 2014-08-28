%extend ISVCDecoder {

	DECODING_STATE DecodeFrame2(const unsigned char* pSrc, const int iSrcLen, STargetPicture* target)
	{	
		return $self->DecodeFrame2(pSrc, iSrcLen, target->ppDst, &target->info);
	}

	DECODING_STATE DecodeFrame2(STargetPicture* target)
	{
		return $self->DecodeFrame2(NULL, 0, target->ppDst, &target->info);
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

