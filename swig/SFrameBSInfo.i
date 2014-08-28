%extend SFrameBSInfo {
	SLayerBSInfo* getSLayerInfo(int frame)
	{
		return &$self->sLayerInfo[frame];

	}
}
