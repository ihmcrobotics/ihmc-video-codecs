%extend TagBufferInfo{
	SSysMEMBuffer* getUsrData()
	{
		return (SSysMEMBuffer*) &$self->UsrData;
	}
}
