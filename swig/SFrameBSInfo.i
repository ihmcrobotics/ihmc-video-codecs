%extend SFrameBSInfo {
	SLayerBSInfo* getSLayerInfo(int frame)
	{
		return &$self->sLayerInfo[frame];

	}

	int getBufferSize()
	{
		int size = 0;
		for(int i = 0; i < $self->iLayerNum; i++)
		{
			for(int n = 0; n < $self->sLayerInfo[i].iNalCount; n++)
			{
				size += $self->sLayerInfo[i].pNalLengthInByte[n];
			}
		}
		return size;
	}

	void getBuffer(unsigned char* buffer)
	{
		int pointer = 0;
		
		for(int i = 0; i < $self->iLayerNum; i++)
		{
			for(int n = 0; n < $self->sLayerInfo[i].iNalCount; n++)
			{
				int layerLength = $self->sLayerInfo[i].pNalLengthInByte[n];
				memcpy($self->sLayerInfo[i].pBsBuf, buffer + pointer, layerLength);
				pointer += layerLength;	
			}
		}
	}
}
