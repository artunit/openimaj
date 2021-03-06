/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

/**
 * A helper class for a common stage type. In this case, a stage that goes from
 * text to a sequence file of bytes indexed by longs
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <INPUT_FORMAT>
 * @param <INPUT_KEY>
 * @param <INPUT_VALUE>
 * @param <MAP_OUTPUT_KEY>
 * @param <MAP_OUTPUT_VALUE>
 * @param <OUTPUT_KEY>
 * @param <OUTPUT_VALUE>
 *
 */
public abstract class MultipleOutputStage<INPUT_FORMAT extends FileInputFormat<INPUT_KEY, INPUT_VALUE>, INPUT_KEY, INPUT_VALUE, MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE>
		extends
			Stage<
			INPUT_FORMAT,
			FileOutputFormat<OUTPUT_KEY, OUTPUT_VALUE>,
			INPUT_KEY, INPUT_VALUE,
			MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE
			>
{

	@Override
	public abstract Class<? extends MultipleOutputReducer<MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE>>
			reducer();

	@Override
	public Job stage(Path[] inputs, Path output, Configuration conf) throws Exception {

		final Job job = super.stage(inputs, output, conf);
		job.setOutputFormatClass(NullOutputFormat.class);
		MultipleOutputs.addNamedOutput(job, "text", TextOutputFormat.class, NullWritable.class, Text.class);
		return job;
	}
}
