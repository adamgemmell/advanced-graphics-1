package com.ajsg2.question1

import com.ajsg2.common.Constants
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.*
import kotlin.math.cos
import kotlin.math.sin


fun main(args: Array<String>) {

    // Set up GLFW window

    val errorCallBack = GLFWErrorCallback.createPrint(System.err)
    GLFW.glfwSetErrorCallback(errorCallBack)
    GLFW.glfwInit()
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)
    val window: Long = GLFW.glfwCreateWindow(Constants.WIDTH, Constants.HEIGHT, "HelloGL", 0, 0)
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
            in vec4 vPosition;

            out vec3 position;

            void main() {
                position = vPosition.xyz;
                gl_Position = vec4(v, 1.0);
            }""".trimMargin()

    val fragmentShader =
            """#version 330

            in vec3 position;

            out vec4 frag_color;

            const vec4 BRICK = vec4(0.5, 0.1, 0.1, 1.0);
            const vec4 MORTAR = vec4(0.7, 0.7, 0.7, 1.0);

            void main() {
                if(position.x < position.y)
                    frag_color = MORTAR;
                else
                    frag_color = BRICK;
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

    val numPoints = 120
    val radius = 0.5f

    // Set up float buffer
    val coords = FloatArray(numPoints*9, { i:Int ->
        when (i % 9) {
            0 -> {
                val ithpoint = i/9
                radius*cos(2*Math.PI * ithpoint/numPoints).toFloat()
            }
            1 -> {
                val ithpoint = (i-1)/9
                radius*sin(2*Math.PI * ithpoint/numPoints).toFloat()
            }
            3 -> {
                val ithpoint = (i-3)/9+1
                radius*cos(2*Math.PI * ithpoint/numPoints).toFloat()
            }
            4 -> {
                val ithpoint = (i-4)/9+1
                radius*sin(2*Math.PI * ithpoint/numPoints).toFloat()
            }
            else -> 0f
        }})

    val fbo = BufferUtils.createFloatBuffer(coords.size)
    fbo.put(coords)
    fbo.flip()      // Mark ready for read

    // Store into a vbo
    val vbo = GL15.glGenBuffers()       // Get name for the VBO
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)    // Activate
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fbo, GL15.GL_STATIC_DRAW)   // Send to GPU

    // position stuff
    val vPosition = GL20.glGetAttribLocation(program, "vPosition")
    GL20.glEnableVertexAttribArray(vPosition)

    // Bind the vbo into a VAO
    val vLoc = GL20.glGetAttribLocation(program, "v")
    val vao = GL30.glGenVertexArrays()          // Get name for the VAO
    GL30.glBindVertexArray(vao)                 // Activate
    GL20.glEnableVertexAttribArray(vLoc)       // Enable attribute 0
    GL20.glVertexAttribPointer(vLoc, 3, GL11.GL_FLOAT, false, 0, 0)

    // Loop
    while (!GLFW.glfwWindowShouldClose(window)) {
        GLFW.glfwPollEvents()

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL30.glBindVertexArray(vao)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numPoints*3)
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