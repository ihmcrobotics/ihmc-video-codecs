/*
Helper code to map outargs for WelsCreateSVC(En/De)Coder to output objects in java
*/

%define OUTARGS(TYPE, VARNAME, OUTTYPE, FUNC)
%newobject FUNC;
// Delete input variable, set to temporary variable
%typemap(in, numinputs=0) TYPE **VARNAME (TYPE* encoder){
        $1 = &encoder;
}

// Change function output to be of type TYPE
%typemap(jstype) OUTTYPE FUNC "$typemap(jstype, TYPE*)"
%typemap(jtype) OUTTYPE FUNC "$typemap(jtype, TYPE*)"
%typemap(jni) OUTTYPE FUNC "$typemap(jni, TYPE*)"
%typemap(javaout) OUTTYPE FUNC "$typemap(javaout, TYPE*)"

// Return pointer to TYPE
%typemap(argout) TYPE **VARNAME {
        *(TYPE**)&$result = *$1;
}
// Check if the output of the FUNC call is zero, if not throw an IOException
%javaexception("java.io.IOException") FUNC {
	$action
	if(result != 0) {
		jclass clazz = JCALL1(FindClass, jenv, "java.io.IOException");
		JCALL2(ThrowNew, jenv, clazz, "An error occured calling FUNC");
		return $null;
	}
}
%enddef

// int WelsCreateSVCEncoder(ISVCEncoder** ppEncoder)
OUTARGS(ISVCEncoder, ppEncoder, int, WelsCreateSVCEncoder)

// long WelsCreateDecoder(ISVCDecoder** ppDecoder)
OUTARGS(ISVCDecoder, ppDecoder, long, WelsCreateDecoder)
