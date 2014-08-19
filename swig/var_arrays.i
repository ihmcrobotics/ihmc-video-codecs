// Hacks to handle variable sized arrays in structs. arg1 can change with SWIG versions, need to find a better solution

%apply unsigned int[] { unsigned int*};
%typemap(out) unsigned int* SliceInformation::pLengthOfSlices {
        $result = SWIG_JavaArrayOutUint(jenv, (unsigned int *)$1, arg1->iCodedSliceCount);
}

%apply int[] { int*  };
%typemap(out) int* SLayerBSInfo::pNalLengthInByte {
        $result = SWIG_JavaArrayOutInt(jenv, (int *)$1, arg1->iNalCount);
}

