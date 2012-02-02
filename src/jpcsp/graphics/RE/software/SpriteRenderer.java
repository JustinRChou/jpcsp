/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.graphics.RE.software;

import jpcsp.graphics.GeContext;
import jpcsp.graphics.VertexState;

/**
 * @author gid15
 *
 */
public class SpriteRenderer extends BasePrimitiveRenderer {
	protected VertexState v1;
	protected VertexState v2;
	protected int sourceDepth;

	public SpriteRenderer(GeContext context, CachedTexture texture, boolean useVertexTexture) {
		init(context, texture, useVertexTexture, false);
	}

	public void setVertex(VertexState v1, VertexState v2) {
		this.v1 = v1;
		this.v2 = v2;
		setVertexPositions(v1, v2);
	}

	@Override
	public boolean prepare() {
        if (log.isTraceEnabled()) {
        	log.trace(String.format("SpriteRenderer"));
        }

        if (!isVisible()) {
        	return false;
        }

        setVertexTextures(v1, v2);

        sourceDepth = (int) v2.p[2];

        return true;
	}

	@Override
	public void render() {
    	float v = prim.vStart;
        for (int y = 0; y < prim.destinationHeight; y++) {
        	pixel.y = prim.pyMin + y;
    		pixel.v = v;
    		float u = prim.uStart;
        	for (int x = 0; x < prim.destinationWidth; x++) {
        		pixel.newPixel();
            	pixel.x = prim.pxMin + x;
        		pixel.u = u;
            	pixel.sourceDepth = sourceDepth;
        		pixel.destination = imageWriter.readCurrent();
        		pixel.destinationDepth = depthWriter.readCurrent();
        		for (int i = 0; i < numberFilters; i++) {
        			filters[i].filter(pixel);
        			if (!pixel.filterPassed) {
        				break;
        			}
        		}
if (isLogTraceEnabled) {
	log.trace(String.format("Pixel (%d,%d), passed=%b, tex (%f, %f), source=0x%08X, dest=0x%08X, prim=0x%08X, sec=0x%08X, sourceDepth=%d, destDepth=%d, filterOnFailed=%s", pixel.x, pixel.y, pixel.filterPassed, pixel.u, pixel.v, pixel.source, pixel.destination, pixel.primaryColor, pixel.secondaryColor, pixel.sourceDepth, pixel.destinationDepth, pixel.filterOnFailed));
}
        		if (pixel.filterPassed) {
        			imageWriter.writeNext(pixel.source);
        			depthWriter.writeNext(pixel.sourceDepth);
    			} else if (pixel.filterOnFailed != null) {
    				// Filter did not pass, but we have a filter to be executed in that case
    				pixel.source = pixel.destination;
    				pixel.filterOnFailed.filter(pixel);
    				imageWriter.writeNext(pixel.source);
    				depthWriter.skip(1);
        		} else {
        			// Filter did not pass, do not update the pixel
        			imageWriter.skip(1);
        			depthWriter.skip(1);
        		}
        		u += prim.uStep;
        	}
        	imageWriter.skip(imageWriterSkipEOL);
        	depthWriter.skip(depthWriterSkipEOL);
        	v += prim.vStep;
        }
        super.render();
	}
}