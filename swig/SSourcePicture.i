%extend Source_Picture_s {
        void setPData(int plane, unsigned char* data)
        {
                $self->pData[plane] = data;
        }

	unsigned char* getPData(int plane)
	{
		return $self->pData[plane];
	}
};

