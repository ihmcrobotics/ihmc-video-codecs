%define SET_ENCODER_OPTION(TYPE)

    #define _FUNC_ Get##TYPE
    long SetOption(ENCODER_OPTION eOptionId, TYPE* option)
	{
		return $self->SetOption(eOptionId, option);
	}

    %newobject Get##TYPE##Option;
	TYPE* Get##TYPE##Option(ENCODER_OPTION eOptionId)
	{
	    TYPE* pOption = new TYPE;
	    $self->GetOption(eOptionId, pOption);
	    return pOption;
	}
%enddef

%extend ISVCEncoder {


    long SetOption(ENCODER_OPTION eOptionId, int option)		
	{
		return $self->SetOption(eOptionId, &option);
	}

	long SetOption(ENCODER_OPTION eOptionId, bool option)
	{
		return $self->SetOption(eOptionId, &option);
	}

    SET_ENCODER_OPTION(SEncParamBase)
    SET_ENCODER_OPTION(SEncParamExt)
    SET_ENCODER_OPTION(SProfileInfo)
    SET_ENCODER_OPTION(SLevelInfo)
    SET_ENCODER_OPTION(SDeliveryStatus)
    SET_ENCODER_OPTION(SLTRRecoverRequest)
    SET_ENCODER_OPTION(SLTRMarkingFeedback)
    SET_ENCODER_OPTION(SDumpLayer)
    SET_ENCODER_OPTION(SBitrateInfo)

	int GetIntOption(ENCODER_OPTION eOptionId)
	{
	    int ret;
	    $self->GetOption(eOptionId, &ret);
	    return ret;
	}

	bool GetBoolOption(ENCODER_OPTION eOptionId)
	{
	    bool ret;
	    $self->GetOption(eOptionId, &ret);
	    return ret;
	}

}
