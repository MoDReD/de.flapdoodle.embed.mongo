/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embedmongo.io;


public class StreamToLineProcessor implements IStreamProcessor {

	private final IStreamProcessor _destination;
	StringBuilder _buffer=new StringBuilder();

	public StreamToLineProcessor(IStreamProcessor destination) {
		_destination = destination;
	}
	
	@Override
	public void process(String block) {
		int newLineEnd=block.indexOf('\n');
		if (newLineEnd==-1) {
			_buffer.append(block);
		} else {
			_buffer.append(block.substring(0,newLineEnd+1));
			_destination.process(getAndClearBuffer());
			do {
				int lastEnd=newLineEnd;
				newLineEnd=block.indexOf('\n',newLineEnd+1);
				if (newLineEnd!=-1) {
					_destination.process(block.substring(lastEnd+1,newLineEnd+1));
				} else {
					_buffer.append(block.substring(lastEnd+1));
				}
			} while (newLineEnd!=-1);
		}
	}

	private String getAndClearBuffer() {
		String ret=_buffer.toString();
		_buffer.setLength(0);
		return ret;
	}
	
	@Override
	public void onProcessed() {
		if (_buffer.length()>0) {
			_destination.process(getAndClearBuffer());
		}
		_destination.onProcessed();
	}

	public static IStreamProcessor wrap(IStreamProcessor destination) {
		return new StreamToLineProcessor(destination);
	}
}
