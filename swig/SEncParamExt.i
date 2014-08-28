%extend TagEncParamExt {
	SSpatialLayerConfig* getSpatialLayer(int layer)
	{
		if(layer >= $self->iSpatialLayerNum)
		{
			return NULL;
		}
		return &$self->sSpatialLayers[layer];
	}
}
		
