/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.icatchtek.nadk.show.kvsarchivedmedialibextend;


import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.json.AwsJsonReader;

/**
 * JSON unmarshaller for POJO DASHFragmentSelector
 */
class DASHFragmentSelectorJsonUnmarshaller implements
        Unmarshaller<DASHFragmentSelector, JsonUnmarshallerContext> {

    public DASHFragmentSelector unmarshall(JsonUnmarshallerContext context) throws Exception {
        AwsJsonReader reader = context.getReader();
        if (!reader.isContainer()) {
            reader.skipValue();
            return null;
        }
        DASHFragmentSelector dASHFragmentSelector = new DASHFragmentSelector();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("FragmentSelectorType")) {
                dASHFragmentSelector.setFragmentSelectorType(StringJsonUnmarshaller.getInstance()
                        .unmarshall(context));
            } else if (name.equals("TimestampRange")) {
                dASHFragmentSelector.setTimestampRange(DASHTimestampRangeJsonUnmarshaller
                        .getInstance()
                        .unmarshall(context));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return dASHFragmentSelector;
    }

    private static DASHFragmentSelectorJsonUnmarshaller instance;

    public static DASHFragmentSelectorJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new DASHFragmentSelectorJsonUnmarshaller();
        return instance;
    }
}
