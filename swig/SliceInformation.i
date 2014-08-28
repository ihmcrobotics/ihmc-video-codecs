%extend SliceInformation {
	int getLengthOfSlices(int slice)
	{
		if(slice >= $self->iCodedSliceCount)
		{
			return -1;
		}
		return $self->pLengthOfSlices[slice];
	}
	
	void getSlice(int slice, unsigned char* target)
	{
		if(slice >= $self->iCodedSliceCount)
                {
                        return;
                }
		int offset;
		for(int i = 0; i < slice; i++)
		{
			offset += $self->pLengthOfSlices[i];
		}
		memcpy(target, $self->pBufferOfSlices + offset, $self->pLengthOfSlices[slice]);
	}
}
