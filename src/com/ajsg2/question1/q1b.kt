package com.ajsg2.question1

import com.ajsg2.common.Constants
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.*
import kotlin.math.cos
import kotlin.math.sin

class q1b {

    fun main(args: Array<String>) {

        // Set up GLFW window

        val errorCallBack = GLFWErrorCallback.createPrint(System.err)
        GLFW.glfwSetErrorCallback(errorCallBack)
        GLFW.glfwInit()
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
        val window: Long = GLFW.glfwCreateWindow(Constants.WIDTH, Constants.HEIGHT, "HelloGL", 0,
                0)
        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(window)

        // Set up OpenGL

        GL.createCapabilities()
        GL11.glClearColor(0.2f, 0.4f, 0.6f, 0.0f)
        GL11.glClearDepth(1.0)

        // Set up minimal shader programs

        val vertexShader =
                """#version 330

            in vec3 v;
            void main() {
                gl_Position = vec4(v, 1.0);
            }""".trimMargin()

        val fragmentShader =
                """#version 330

            out vec4 frag_color;
            void main() {
                frag_color = vec4(1.0, 1.0, 1.0, 1.0);
            }""".trimMargin()

        // Compile vertex shader
        val vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vs, vertexShader)
        GL20.nglCompileShader(vs)

        // Compile fragment shader
        val fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fs, fragmentShader)
        GL20.glCompileShader(fs)

        // Link vertex and fragment shaders into an active program
        val program = GL20.glCreateProgram()
        GL20.glAttachShader(program, vs)
        GL20.glAttachShader(program, fs)
        GL20.glLinkProgram(program)
        GL20.glUseProgram(program)

        // Set up data

        val numPoints = 360

        // Set up float buffer
        val coords = FloatArray(numPoints*2, { i ->
            if (i%2 == 0)
                cos(2*Math.PI * i/(2*numPoints)).toFloat()
            else
                sin(2*Math.PI * (i-1)/(2*numPoints)).toFloat()} )

        val fbo = BufferUtils.createFloatBuffer(coords.size)
        fbo.put(coords)
        fbo.flip()      // Mark ready for read

        // Store into a vbo
        val vbo = GL15.glGenBuffers()       // Get name for the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)    // Activate
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fbo, GL15.GL_STATIC_DRAW)   // Send to GPU

        // Bind the vbo into a VAO
        val vao = GL30.glGenVertexArrays()          // Get name for the VAO
        GL30.glBindVertexArray(vao)                 // Activate
        GL20.glEnableVertexAttribArray(0)       // Enable attribute 0
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
        // Link VBO to VAO attr 0

        // Loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents()

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            GL30.glBindVertexArray(vao)
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
            // Start at 0, num vertices 3

            GLFW.glfwSwapBuffers(window)
        }

        // Clean up

        GL15.glDeleteBuffers(vbo)
        GL30.glDeleteVertexArrays(vao)
        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null).free()
    }
}