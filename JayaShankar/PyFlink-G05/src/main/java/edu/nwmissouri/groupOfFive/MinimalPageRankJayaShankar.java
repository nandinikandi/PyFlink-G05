/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.nwmissouri.groupOfFive;

// beam-playground:
//   name: MinimalWordCount
//   description: An example that counts words in Shakespeare's works.
//   multifile: false
//   default_example: true
//   context_line: 71
//   categories:
//     - Combiners
//     - Filtering
//     - IO
//     - Core Transforms
//     - Quickstart

import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * An example that counts words in Shakespeare.
 *
 * <p>This class, {@link MinimalPageRankJayaShankar}, is the first in a series of four successively more
 * detailed 'word count' examples. Here, for simplicity, we don't show any error-checking or
 * argument processing, and focus on construction of the pipeline, which chains together the
 * application of core transforms.
 *
 * <p>Next, see the {@link WordCount} pipeline, then the {@link DebuggingWordCount}, and finally the
 * {@link WindowedWordCount} pipeline, for more detailed examples that introduce additional
 * concepts.
 *
 * <p>Concepts:
 *
 * <pre>
 *   1. Reading data from text files
 *   2. Specifying 'inline' transforms
 *   3. Counting items in a PCollection
 *   4. Writing data to text files
 * </pre>
 *
 * <p>No arguments are required to run this pipeline. It will be executed with the DirectRunner. You
 * can see the results in the output files in your current working directory, with names like
 * "wordcounts-00001-of-00005. When running on a distributed service, you would use an appropriate
 * file service.
 */
public class MinimalPageRankJayaShankar {

  public static void main(String[] args) {
    
    PipelineOptions options = PipelineOptionsFactory.create();

    
    Pipeline p = Pipeline.create(options);
    
    String dataFolder = "web04";
   
   
   PCollection<KV<String,String>> js1 = JayaShankarManginaMapper01(p,"go.md",dataFolder);
   
   PCollection<KV<String,String>> js2 = JayaShankarManginaMapper01(p,"python.md",dataFolder);
   
   PCollection<KV<String,String>> js3 = JayaShankarManginaMapper01(p,"java.md",dataFolder);
   
   PCollection<KV<String,String>> js4 = JayaShankarManginaMapper01(p,"README.md",dataFolder);

   PCollection<KV<String,String>> js5 = JayaShankarManginaMapper01(p,"erlang.md",dataFolder);
   
   PCollectionList<KV<String, String>> pCollectionList = PCollectionList.of(js1).and(js2).and(js3).and(js4).and(js5);
  
   PCollection<KV<String, String>> mergedList = pCollectionList.apply(Flatten.<KV<String,String>>pCollections());

   PCollection<KV<String, Iterable<String>>> groupedList =mergedList.apply(GroupByKey.create());
  
   PCollection<String> pLinksString = groupedList.apply(MapElements.into(TypeDescriptors.strings()).via((mergeOut)->mergeOut.toString()));
   
   pLinksString.apply(TextIO.write().to("JayaShankarPR"));  
   
   p.run().waitUntilFinish();
  
  }

  public static PCollection<KV<String,String>> JayaShankarManginaMapper01(Pipeline p, String filename, String dataFolder){
   
    String newdataPath = dataFolder + "/" + filename;
    
    PCollection<String> pcolInput = p.apply(TextIO.read().from(newdataPath));
    
    PCollection<String> pcollinkLines = pcolInput.apply(Filter.by((String line) -> line.startsWith("[")));
     
    PCollection<String> pcolLinks = pcollinkLines.apply(MapElements.into((TypeDescriptors.strings()))
    .via((String linkLine) ->linkLine.substring(linkLine.indexOf("(")+1, linkLine.length()-1)));
     
    PCollection<KV<String,String>> pColKVPairs =  pcolLinks.apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
    .via((String outLink) -> KV.of(filename,outLink)));
    
    return pColKVPairs;
  }
}
