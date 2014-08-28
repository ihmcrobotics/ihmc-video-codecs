ISVCEncoder* WelsCreateSVCEncoder()
{
	ISVCEncoder* pEncoder;
	if(WelsCreateSVCEncoder(&pEncoder) != 0)
	{
		return NULL;
	}
	return pEncoder;
}

ISVCDecoder* WelsCreateDecoder()
{
	ISVCDecoder* pDecoder;
	if(WelsCreateDecoder(&pDecoder) != 0)
	{
		return NULL;
	}
	return pDecoder;
}
