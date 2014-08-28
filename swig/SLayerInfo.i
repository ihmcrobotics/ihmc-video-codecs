%extend SLayerBSInfo {
	
	int getPBsBufSize()
	{
	   int layerSize = 0;
           for (int j = 0; j < $self->iNalCount; ++j) {
              layerSize += $self->pNalLengthInByte[j];
          }
	   return layerSize;
	}

	void getPBsBuf(unsigned char* target)
	{
	
	    int layerSize = 0;
            for (int j = 0; j < $self->iNalCount; ++j) {
              layerSize += $self->pNalLengthInByte[j];
            }
	    memcpy(target, $self->pBsBuf, layerSize);
	
	}
	
	int getNalLengthInByte(int nal)
	{	
		if(nal >= $self->iNalCount)
		{
			return -1;
		}
		return $self->pNalLengthInByte[nal];
	}

	void getNal(int nal, unsigned char* target)
	{
		if(nal >= $self->iNalCount)
		{
			return;
		}
		int offset = 0;
		for(int j = 0; j < nal; j++)
		{
			offset += $self->pNalLengthInByte[j];
		}
		memcpy(target, $self->pBsBuf + offset, $self->pNalLengthInByte[nal]);
	}

}
