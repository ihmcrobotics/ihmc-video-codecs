%extend SLayerBSInfo {
	
	int getLayerSize()
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

}
