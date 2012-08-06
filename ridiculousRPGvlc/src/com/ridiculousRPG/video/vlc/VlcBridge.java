package com.ridiculousRPG.video.vlc;

public class VlcBridge {
	private boolean debug;

	/*JNI
	#include <vlc/vlc.h>
	// Audio prerender callback
	void prepareAudio(void* p_audio_data, uint8_t** pp_pcm_buffer , unsigned int size);
	// Audio postrender callback
	void handleAudio(void* p_audio_data, uint8_t* p_pcm_buffer, unsigned int channels, unsigned int rate, unsigned int nb_samples, unsigned int bits_per_sample, unsigned int size, int64_t pts);
	// Video prerender and postrender callbacks not implemented.
	 */
	public void startBackgroundVlc() {
		startBackgroundVlcNative(debug);
	}
	private native void startBackgroundVlcNative(boolean debug); /*
		// VLC pointers
		libvlc_instance_t *vlcInstance;
		libvlc_media_player_t *mp;
		libvlc_media_t *media;

		// VLC options
		char smem_options[256];

		// We are using transcode because smem only support raw audio and video formats
		// We print (as a decimal value) the addresses. Note that you can also define smem-audio-data : this pointer
		// will be passed to your callbacks (it may be useful to retrive some extra informations) but isn't required at all.
		sprintf(smem_options, "#transcode{acodec=s16l}:smem{audio-postrender-callback=%lld,audio-prerender-callback=%lld}",
			(long long int)(intptr_t)(void*)&handleAudio, (long long int)(intptr_t)(void*)&prepareAudio);

		const char* const* vlc_argv;
		int vlc_argc;
		if (debug) {
			const char* vlc_args[] = {
				"-I", "dummy", // Don't use any interface
				"--ignore-config", // Don't use VLC's config
				"--extraintf=logger", // Log anything
				"--verbose=2", // Be much more verbose then normal for debugging purpose
				"--sout", smem_options // Stream to memory
			};
			vlc_argv = vlc_args;
			vlc_argc = sizeof(vlc_args) / sizeof(vlc_args[0]);
		} else {
			const char* vlc_args[] = {
				"-I", "dummy", // Don't use any interface
				"--ignore-config", // Don't use VLC's config
				"--sout", smem_options // Stream to memory
			};
			vlc_argv = vlc_args;
			vlc_argc = sizeof(vlc_args) / sizeof(vlc_args[0]);
		}

		// We launch VLC
		vlcInstance = libvlc_new(vlc_argc, vlc_argv);
		printf("size: %ld", vlc_argc);

		mp = libvlc_media_player_new(vlcInstance);
	*/
	/*JNI
		void prepareAudio(void* p_audio_data, uint8_t** pp_pcm_buffer , unsigned int size) {
		// TODO: Lock the mutex
		//*pp_pcm_buffer = // TODO
		}
		void handleAudio(void* p_audio_data, uint8_t* p_pcm_buffer, unsigned int channels, unsigned int rate, unsigned int nb_samples, unsigned int bits_per_sample, unsigned int size, int64_t pts ) {
		// TODO: explain how data should be handled
		// TODO: Unlock the mutex
		}
	*/

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
