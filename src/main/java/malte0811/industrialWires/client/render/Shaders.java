/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2018 malte0811
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.client.render;

import blusunrize.immersiveengineering.api.IEApi;
import malte0811.industrialWires.IWConfig;
import malte0811.industrialWires.IndustrialWires;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Shaders
{
	public static int JACOBS_ARC;
	public static int MARX;
	private static String PREFIX = "/assets/"+ IndustrialWires.MODID+"/shaders/";
	public static void initShaders(boolean setupReload) {
		if (areShadersEnabled()) {
			JACOBS_ARC = createProgram(null, PREFIX + "jacobs.frag");
			MARX = createProgram(null, PREFIX + "marx.frag");

			if (setupReload) {
				IEApi.renderCacheClearers.add(() -> {
					if (JACOBS_ARC != 0) {
						deleteShader(JACOBS_ARC);
						JACOBS_ARC = 0;
					}
					if (MARX != 0) {
						deleteShader(MARX);
						MARX = 0;
					}

					initShaders(false);
				});
			}
		}
	}
	//All stolen from Botania...
	private static final int VERT = ARBVertexShader.GL_VERTEX_SHADER_ARB;
	private static final int FRAG = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;


	private static void deleteShader(int id) {
		if (id != 0) {
			ARBShaderObjects.glDeleteObjectARB(id);
		}
	}

	public static void useShader(int shader) {
		if (areShadersEnabled()) {
			ARBShaderObjects.glUseProgramObjectARB(shader);

			if (shader != 0) {
				int time = ARBShaderObjects.glGetUniformLocationARB(shader, "time");
				ARBShaderObjects.glUniform1fARB(time, Minecraft.getMinecraft().world.getTotalWorldTime() + Minecraft.getMinecraft().getRenderPartialTicks());
			}
		}
	}

	public static void stopUsingShaders() {
		useShader(0);
	}
	private static int createProgram(String vert, String frag) {
		int vertId = 0, fragId = 0, program;
		if(vert != null)
			vertId = createShader(vert, VERT);
		if(frag != null)
			fragId = createShader(frag, FRAG);

		program = ARBShaderObjects.glCreateProgramObjectARB();
		if(program == 0)
			return 0;

		if(vert != null)
			ARBShaderObjects.glAttachObjectARB(program, vertId);
		if(frag != null)
			ARBShaderObjects.glAttachObjectARB(program, fragId);

		ARBShaderObjects.glLinkProgramARB(program);
		if(ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
			IndustrialWires.logger.error(getLogInfo(program));
			return 0;
		}

		ARBShaderObjects.glValidateProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
			IndustrialWires.logger.error(getLogInfo(program));
			return 0;
		}

		return program;
	}

	private static int createShader(String filename, int shaderType){
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if(shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		}
		catch(Exception e) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			e.printStackTrace();
			return -1;
		}
	}

	private static String getLogInfo(int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	private static String readFileAsString(String filename) throws Exception {
		InputStream in = Shaders.class.getResourceAsStream(filename);

		if(in == null)
			return "";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
			return preProcess(reader.lines());
		}
	}

	public static boolean areShadersEnabled() {
		return IWConfig.HVStuff.enableShaders && OpenGlHelper.shadersSupported;
	}
	/*
	 * Custom PreProcessor, I need random a lot and wanted #include
	 */
	private static String preProcess(Stream<String> lines) {
		lines = lines.map((s)->{
			if (s.startsWith("#include ")) {
				s = s.substring("#include ".length());
				String fileName = PREFIX +s;
				try {
					return readFileAsString(fileName);
				} catch (Exception e) {
					throw new RuntimeException(fileName+" not readable", e);
				}
			}
			return s;
		});
		return lines.collect(Collectors.joining("\n"));
	}
}
