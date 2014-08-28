%extend Source_Picture_s {
        void setPData(int plane, unsigned char* data)
        {
		if(plane > 3)
		{
			return;
		}
                $self->pData[plane] = data;
        }
};

