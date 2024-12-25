# angle-mod

This is a fabric mod that forces Minecraft to use GLES3+EGL instead of Desktop OpenGL.

## Requirements

- [A patched version of glfw](https://github.com/LWJGL-CI/glfw)
- [Mesa](https://mesa.freedesktop.org/) with EGL support, or [libANGLE](https://github.com/google/angle) (with the
  vulkan backend)
- [Fabric](https://fabricmc.net/)
- Minecraft 1.21.4