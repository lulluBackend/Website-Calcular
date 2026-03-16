class RadioPlayer {
    constructor() {
        
        //ID
        this.liveVideoId = 'tjne3WWB3kA'; 
        this.player = null;
        this.isPlaying = false;
        this.init();
    }

    async init() {
        await this.loadYouTubeAPI();
        //pequeno delay para garantir que a página carregou
        setTimeout(() => this.createPlayer(), 1000);
    }

    loadYouTubeAPI() {
        return new Promise((resolve) => {
            if (window.YT) {
                resolve();
                return;
            }
            const tag = document.createElement('script');
            tag.src = 'https://www.youtube.com/iframe_api';
            const firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
            
            window.onYouTubeIframeAPIReady = () => {
                console.log('✅ YouTube API carregada');
                resolve();
            };
        });
    }

    createPlayer() {
        // Remove player anterior se existir
        if (this.player) {
            this.player.destroy();
        }

        
        const oldDiv = document.getElementById('youtube-radio-player');
        if (oldDiv) oldDiv.remove();

        // Cria div para o player (invisível)
        const playerDiv = document.createElement('div');
        playerDiv.id = 'youtube-radio-player';
        playerDiv.style.display = 'none';
        document.body.appendChild(playerDiv);

        console.log('🎵 Criando player com vídeo:', this.liveVideoId);

        // Inicializa o player com configurações que ajudam o autoplay
        this.player = new YT.Player('youtube-radio-player', {
            videoId: this.liveVideoId,
            playerVars: {
                autoplay: 1,
                controls: 0,
                mute: 1, // Começa mudo para garantir autoplay
                modestbranding: 1,
                rel: 0,
                showinfo: 0,
                enablejsapi: 1,
                origin: window.location.origin
            },
            events: {
                onReady: (event) => {
                    console.log('✅ Player pronto');
                    
                    setTimeout(() => {
                        if (this.player && this.player.unMute) {
                            this.player.unMute();
                            this.player.setVolume(30); // Volume 30%
                            console.log('🔊 Volume ativado');
                        }
                    }, 1000);
                    this.addVolumeControl();
                },
                onStateChange: (event) => {
                    console.log('📺 Estado do player:', event.data);
                    
                    if (event.data === 1) {
                        console.log('▶️ Vídeo está tocando');
                        this.isPlaying = true;
                    }
                },
                onError: (error) => {
                    console.error('❌ Erro no player:', error);
                    
                    setTimeout(() => this.createPlayer(), 5000);
                }
            }
        });
    }

    addVolumeControl() {
        // Remove controle antigo se existir
        const oldControl = document.getElementById('radio-volume-control');
        if (oldControl) oldControl.remove();

        const control = document.createElement('div');
        control.id = 'radio-volume-control';
        control.innerHTML = `
            <div class="radio-control">
                <span class="radio-icon">🎵</span>
                <span class="radio-status">🔴 Cherry Nightcore</span>
                <input type="range" id="radio-volume" min="0" max="100" value="30">
                <button id="radio-toggle" class="radio-toggle" title="Ligar/Desligar">⏸️</button>
            </div>
        `;
        document.body.appendChild(control);

        this.addStyles();

        // Controle de volume
        const volumeSlider = document.getElementById('radio-volume');
        volumeSlider.addEventListener('input', (e) => {
            if (this.player && this.player.setVolume) {
                const vol = parseInt(e.target.value);
                this.player.setVolume(vol);
                console.log('🔊 Volume:', vol);
            }
        });

        // Botão liga/desliga
        const toggleBtn = document.getElementById('radio-toggle');
        toggleBtn.addEventListener('click', () => {
            if (this.player) {
                if (this.isPlaying) {
                    this.player.pauseVideo();
                    toggleBtn.textContent = '▶️';
                    this.isPlaying = false;
                } else {
                    this.player.playVideo();
                    toggleBtn.textContent = '⏸️';
                    this.isPlaying = true;
                }
            }
        });
    }

    addStyles() {
        if (document.getElementById('radio-styles')) return;

        const style = document.createElement('style');
        style.id = 'radio-styles';
        style.textContent = `
            #radio-volume-control {
                position: fixed;
                bottom: 100px;
                right: 20px;
                background: rgba(0, 0, 0, 0.85);
                backdrop-filter: blur(10px);
                padding: 12px 20px;
                border-radius: 50px;
                border: 1px solid rgba(255, 255, 255, 0.15);
                z-index: 9999;
                animation: slideIn 0.5s ease;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
            }
            .radio-control {
                display: flex;
                align-items: center;
                gap: 15px;
                color: white;
            }
            .radio-icon {
                font-size: 20px;
                animation: pulse 2s infinite;
            }
            .radio-status {
                font-size: 13px;
                color: #ff6b6b;
                font-weight: bold;
                white-space: nowrap;
                text-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
            }
            .radio-toggle {
                background: rgba(255, 255, 255, 0.15);
                border: none;
                color: white;
                width: 32px;
                height: 32px;
                border-radius: 50%;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 16px;
                transition: all 0.3s;
                border: 1px solid rgba(255, 255, 255, 0.2);
            }
            .radio-toggle:hover {
                background: rgba(255, 255, 255, 0.25);
                transform: scale(1.1);
            }
            #radio-volume {
                width: 90px;
                height: 4px;
                -webkit-appearance: none;
                background: rgba(255, 255, 255, 0.2);
                border-radius: 4px;
                outline: none;
                cursor: pointer;
            }
            #radio-volume::-webkit-slider-thumb {
                -webkit-appearance: none;
                width: 16px;
                height: 16px;
                background: #ff6b6b;
                border-radius: 50%;
                cursor: pointer;
                transition: transform 0.2s;
                box-shadow: 0 0 10px rgba(255, 107, 107, 0.5);
            }
            #radio-volume::-webkit-slider-thumb:hover {
                transform: scale(1.2);
            }
            @keyframes slideIn {
                from {
                    opacity: 0;
                    transform: translateX(100px);
                }
                to {
                    opacity: 1;
                    transform: translateX(0);
                }
            }
            @keyframes pulse {
                0% { opacity: 1; }
                50% { opacity: 0.5; }
                100% { opacity: 1; }
            }
        `;
        document.head.appendChild(style);
    }
}

// Inicializa quando a página carregar
document.addEventListener('DOMContentLoaded', () => {
    window.radioPlayer = new RadioPlayer();
});