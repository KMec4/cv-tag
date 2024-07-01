package engine;

import static org.lwjgl.vulkan.VK13.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRDisplaySwapchain.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.NVGLSLShader.*;
//import static org.lwjgl.vulkan.KHRVideoQueue.*;
import static org.lwjgl.vulkan.EXTImageDrmFormatModifier.*;
import static org.lwjgl.vulkan.KHRGlobalPriority.*;
import static org.lwjgl.vulkan.EXTFullScreenExclusive.*;
import static org.lwjgl.vulkan.KHRDeferredHostOperations.*;
//import static org.lwjgl.vulkan.EXTImageCompressionControl.*;
//import static org.lwjgl.vulkan.KHRMaintenance1.*;

public class VkResultDecoder
{

    public static String decode(int vkResult)
    {
        switch (vkResult)
        {
            case VK_SUCCESS:
                return "VK_SUCCESS";

            case VK_NOT_READY:
                return "VK_NOT_READY";

            case VK_TIMEOUT:
                return "VK_TIMEOUT";

            case VK_EVENT_SET:
                return "VK_EVENT_SET";
            
            case VK_EVENT_RESET:
                return "VK_EVENT_RESET";

            case VK_INCOMPLETE:
                return "VK_INCOMPLETE";
            
            case VK_ERROR_OUT_OF_HOST_MEMORY:
                return "VK_ERROR_OUT_OF_HOST_MEMORY";
            
            case VK_ERROR_OUT_OF_DEVICE_MEMORY:
                return "VK_ERROR_OUT_OF_DEVICE_MEMORY";
            
            case VK_ERROR_INITIALIZATION_FAILED:
                return "VK_ERROR_INIRIALIZATION_FAILD";
            
            case VK_ERROR_DEVICE_LOST:
                return "VK_ERROR_DEVICE_LOST";
            
            case VK_ERROR_MEMORY_MAP_FAILED:
                return "VK_ERROR_MEMORY_MAP_FAILED";
            
            case VK_ERROR_LAYER_NOT_PRESENT:
                return "VK_ERROR_LAYER_NOT_PRESENT";
            
            case VK_ERROR_EXTENSION_NOT_PRESENT:
                return "VK_ERROR_EXTENTION_NOT_PRESENT";
            
            case VK_ERROR_FEATURE_NOT_PRESENT:
                return "VK_ERROR_EXTENTION_NOT_PRESENT";
            
            case VK_ERROR_INCOMPATIBLE_DRIVER:
                return "VK_ERROR_INCOMPATIBLE_DRIVER";
            
            case VK_ERROR_TOO_MANY_OBJECTS:
                return "VK_ERROR_TOO_MANY_OBJECTS";
            
            case VK_ERROR_FORMAT_NOT_SUPPORTED:
                return "VK_ERROR_FORMAT_NOT_SUPPORTED";
            
            case VK_ERROR_FRAGMENTED_POOL:
                return "VK_ERROR_FRAGMENTED_POOL";
            
            case VK_ERROR_UNKNOWN:
                return "VK_ERROR_UNKNOWN";
            
            case VK_ERROR_OUT_OF_POOL_MEMORY:
                return "VK_ERROR_OUT_OF_POOL_MEMORY";
            
            case VK_ERROR_INVALID_EXTERNAL_HANDLE:
                return "VK_ERROR_INVALID_EXTERNAL_HANDLE";
            
            case VK_ERROR_FRAGMENTATION:
                return "VK_ERROR_FRAGMENTATION";
            
            case VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS:
                return "VK_ERROR_INVALID_OPAQUE_CAPTURE_ADRESS";
            
            case VK_PIPELINE_COMPILE_REQUIRED:                  // 1000297000    -- VK 1.3
                return "VK_PIELINE_COMPILE_REQUIRED";
            
            case VK_ERROR_SURFACE_LOST_KHR:                     // -1000000000   --VK_KHR_surface
                return "VK_ERROR_SURFACE_LOST_KHR";

            case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:             // -1000000001  --VK_KHR_surface
                return "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR";
            
            case VK_SUBOPTIMAL_KHR:                             // 1000001003 --VK_KHR_swapchain
                return "VK_SUBOPTIMAL_KHR";
            
            case VK_ERROR_OUT_OF_DATE_KHR:                      // -1000001004 --VK_KHR_swapchain
                return "VK_ERROR_OUT_OF_DATE_KHR";
            
            case VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:             // -1000003001  --VK_KHR_display_swapchain
                return "VK_ERROR_INCOMPATIBLE_DISPLAY_KHR";
            
            case VK_ERROR_VALIDATION_FAILED_EXT:                // -1000011001  --VK_EXT_debug_report
                return "VK_ERROR_VALIDATION_FAILED_EXT";
            
            case VK_ERROR_INVALID_SHADER_NV:                    // -1000012000  --VK_NV_glsl_shader
                return "VK_ERROR_INVALID_SHADER_NV";
            
            /*
            case VK_ERROR_IMAGE_USAGE_NOT_SUPPORTED_KHR:             // -1000023000  --VK_KHR_video_queue
                return "VK_ERROR_IMAGE_USAGE_NOT_SUPPORTED_KHR";
            
            case VK_ERROR_VIDEO_PICTURE_LAYOUT_NOT_SUPPORTED_KHR:    // -1000023001  --VK_KHR_video_queue
                return "VK_ERROR_VIDEO_PICTURE_LAYOUT_NOT_SUPPORTED_KHR";
            
            case VK_ERROR_VIDEO_PROFILE_OPERATION_NOT_SUPPORTED_KHR: // -1000023002  --VK_KHR_video_queue
                return "VK_ERROR_VIDEO_PROFILE_OPERATION_NOT_SUPPORTED_KHR";
            
            case VK_ERROR_VIDEO_PROFILE_FORMAT_NOT_SUPPORTED_KHR:    // -1000023003  --VK_KHR_video_queue
                return "VK_ERROR_VIDEO_PROFILE_FORMAT_NOT_SUPPORTED_KHR";
            
            case VK_ERROR_VIDEO_PROFILE_CODEC_NOT_SUPPORTED_KHR:     // -1000023004  --VK_KHR_video_queue
                return "VK_ERROR_VIDEO_PROFILE_CODEC_NOT_SUPPORTED_KHR";
            
            case  VK_ERROR_VIDEO_STD_VERSION_NOT_SUPPORTED_KHR:      // -1000023005  --VK_KHR_video_queue
                return "VK_ERROR_VIDEO_STD_VERSION_NOT_SUPPORTED_KHR";
            */
            
            case VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT:   // -1000158000  --VK_EXT_image_drm_format_modifier
                return "VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT";
            
            case VK_ERROR_NOT_PERMITTED_KHR:                    // -1000174001  --VK_KHR_global_priority
                return "VK_ERROR_NOT_PERMITTED_KHR";
            
            case VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT:  // -1000255000  --VK_EXT_full_screen_exclusive
                return "VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT";
            
            case VK_THREAD_IDLE_KHR:                            // 1000268000  --VK_KHR_deferred_host_operations
                return "VK_THREAD_IDLE_KHR";
            
            case VK_THREAD_DONE_KHR:                            // 1000268001  --VK_KHR_deferred_host_operations
                return "VK_THREAD_DONE_KHR";
            
            case VK_OPERATION_DEFERRED_KHR:                     // 1000268002  --VK_KHR_deferred_host_operations
                return "VK_OPERATION_DEFERRED_KHR";
            
            case VK_OPERATION_NOT_DEFERRED_KHR:                 // 1000268003  --VK_KHR_deferred_host_operations
                return "VK_OPERATION_NOT_DEFERRED_KHR";
            
            /*
            case VK_ERROR_COMPRESSION_EXHAUSTED_EXT:            // -1000338000  --VK_EXT_image_compression_control
                return "VK_ERROR_COMPRESSION_EXHAUSTED_EXT";
            */

            // ------------------------------ DUPLICATED CASES ------------------------------- \\
            
            /*
            case VK_ERROR_OUT_OF_POOL_MEMORY_KHR:               // VK_ERROR_OUT_OF_POOL_MEMORY  --VK_KHR_maintenance1
                return "VK_ERROR_OUT_OF_POOL_MEMORY_KHR";

              // Provided by VK_KHR_external_memory
                VK_ERROR_INVALID_EXTERNAL_HANDLE_KHR = VK_ERROR_INVALID_EXTERNAL_HANDLE,
              // Provided by VK_EXT_descriptor_indexing
                VK_ERROR_FRAGMENTATION_EXT = VK_ERROR_FRAGMENTATION,
              // Provided by VK_EXT_global_priority
                VK_ERROR_NOT_PERMITTED_EXT = VK_ERROR_NOT_PERMITTED_KHR,
              // Provided by VK_EXT_buffer_device_address
                VK_ERROR_INVALID_DEVICE_ADDRESS_EXT = VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS,
              // Provided by VK_KHR_buffer_device_address
                VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS_KHR = VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS,
              // Provided by VK_EXT_pipeline_creation_cache_control
                VK_PIPELINE_COMPILE_REQUIRED_EXT = VK_PIPELINE_COMPILE_REQUIRED,
              // Provided by VK_EXT_pipeline_creation_cache_control
                VK_ERROR_PIPELINE_COMPILE_REQUIRED_EXT = VK_PIPELINE_COMPILE_REQUIRED,*/


            default:
                return "" + vkResult;
        }

    }
    
}
