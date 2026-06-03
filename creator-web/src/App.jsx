import { useState, useEffect, useRef, useMemo } from 'react';
import './App.css';

const DEFAULT_GLSL_SHADER = `precision mediump float;
varying vec2 v_TexCoord;

uniform vec2 u_Resolution;
uniform float u_Time;
uniform float u_TimeOfDay; // 0 to 24
uniform vec2 u_SunPos;
uniform vec2 u_MoonPos;
uniform vec3 u_SunColor;
uniform vec3 u_MoonColor;
uniform float u_SunSize;
uniform float u_MoonSize;
uniform sampler2D u_HorizonTexture;
uniform float u_NightBlend;

uniform float u_SunGlow;
uniform float u_MoonGlow;
uniform float u_StarsEnabled;
uniform float u_StarsDensity;
uniform float u_AtmosphereIntensity;

// Interpolate horizon height by sampling the 1D luminance texture
float getHorizonHeight(float x) {
  return texture2D(u_HorizonTexture, vec2(x, 0.5)).r;
}

void main() {
  vec2 uv = v_TexCoord;
  
  // Sky base colors based on daylight time
  vec3 nightColor = vec3(0.01, 0.02, 0.05);
  vec3 sunriseColor = vec3(0.88, 0.43, 0.28);
  vec3 noonColor = vec3(0.22, 0.52, 0.90);
  vec3 sunsetColor = vec3(0.84, 0.27, 0.29);
  
  vec3 skyColor = nightColor;
  
  if (u_TimeOfDay >= 5.0 && u_TimeOfDay < 7.0) {
    float t = (u_TimeOfDay - 5.0) / 2.0;
    skyColor = mix(mix(nightColor, vec3(0.17, 0.20, 0.31), uv.y), mix(nightColor, sunriseColor, uv.y), t);
  } else if (u_TimeOfDay >= 7.0 && u_TimeOfDay < 11.0) {
    float t = (u_TimeOfDay - 7.0) / 4.0;
    skyColor = mix(mix(nightColor, sunriseColor, uv.y), mix(noonColor, vec3(0.55, 0.75, 0.95), uv.y), t);
  } else if (u_TimeOfDay >= 11.0 && u_TimeOfDay < 15.0) {
    skyColor = mix(noonColor, vec3(0.55, 0.75, 0.95), uv.y);
  } else if (u_TimeOfDay >= 15.0 && u_TimeOfDay < 18.0) {
    float t = (u_TimeOfDay - 15.0) / 3.0;
    skyColor = mix(mix(noonColor, vec3(0.55, 0.75, 0.95), uv.y), mix(sunsetColor, vec3(0.92, 0.63, 0.50), uv.y), t);
  } else if (u_TimeOfDay >= 18.0 && u_TimeOfDay < 20.0) {
    float t = (u_TimeOfDay - 18.0) / 2.0;
    skyColor = mix(mix(sunsetColor, vec3(0.92, 0.63, 0.50), uv.y), nightColor, t);
  } else {
    skyColor = mix(nightColor, vec3(0.02, 0.04, 0.08), uv.y);
  }
  
  // Blend sky colors based on atmosphere intensity
  skyColor = mix(nightColor, skyColor, u_AtmosphereIntensity);
  
  // Render stars at night
  if (u_StarsEnabled > 0.5 && u_NightBlend > 0.0) {
    float starPattern = fract(sin(dot(uv.xy, vec2(12.9898, 78.233))) * 43758.5453);
    if (starPattern > (1.0 - 0.015 * u_StarsDensity)) {
      float blink = 0.5 + 0.5 * sin(u_Time * 3.0 + starPattern * 10.0);
      skyColor += vec3(0.9, 0.95, 1.0) * blink * u_NightBlend * (1.0 - uv.y);
    }
  }
  
  // Render celestial bodies (Sun)
  float distToSun = distance(uv, u_SunPos);
  float sunMask = smoothstep(u_SunSize, u_SunSize - 0.015, distToSun);
  float sunGlow = exp(-distToSun * 12.0) * u_SunGlow;
  skyColor = mix(skyColor, u_SunColor, sunMask + sunGlow * (1.0 - u_NightBlend));
  
  // Render celestial bodies (Moon)
  float distToMoon = distance(uv, u_MoonPos);
  float moonMask = smoothstep(u_MoonSize, u_MoonSize - 0.015, distToMoon);
  float moonGlow = exp(-distToMoon * 15.0) * u_MoonGlow;
  float crescentMask = smoothstep(u_MoonSize, u_MoonSize - 0.015, distance(uv - vec2(0.01, 0.005), u_MoonPos));
  skyColor = mix(skyColor, u_MoonColor, max(0.0, moonMask - crescentMask) + moonGlow * u_NightBlend);
  
  // Render Horizon
  float horizonHeight = getHorizonHeight(uv.x);
  float horizonMask = smoothstep(horizonHeight + 0.003, horizonHeight - 0.003, uv.y);
  vec3 groundColor = mix(vec3(0.02, 0.03, 0.05), vec3(0.05, 0.06, 0.08), uv.y / horizonHeight);
  skyColor = mix(skyColor, groundColor, horizonMask);

  gl_FragColor = vec4(skyColor, 1.0);
}`;

const DEFAULT_LAYERS = [
  {
    texturePath: 'warrior/warrior1.webp',
    offsetX: 0,
    offsetY: 0.1,
    scaleX: 1.0,
    scaleY: 1.0,
    motionType: 'STATIC',
    motionSpeed: 1,
    motionAmplitude: 0,
    motionDirection: 0,
    motionDuration: 5,
    motionStartX: 0,
    motionStartY: 0,
    motionEndX: 0.2,
    motionEndY: 0,
    parallaxStrengthX: 0.05,
    parallaxStrengthY: 0.03,
    nightTintFactor: 0.5,
    opacity: 1.0,
    localUrl: null
  },
  {
    texturePath: 'warrior/warrior2.webp',
    offsetX: 0,
    offsetY: -0.1,
    scaleX: 1.0,
    scaleY: 1.0,
    motionType: 'WAVE',
    motionSpeed: 3,
    motionAmplitude: 0.05,
    motionDirection: 90,
    motionDuration: 5,
    motionStartX: 0,
    motionStartY: 0,
    motionEndX: 0.35,
    motionEndY: 0,
    parallaxStrengthX: 0.08,
    parallaxStrengthY: 0.04,
    nightTintFactor: 0.6,
    opacity: 1.0,
    localUrl: null
  }
];

const DEFAULT_HORIZON = [
  { id: 1, x: 0.0, y: 0.16 },
  { id: 2, x: 0.33, y: 0.20 },
  { id: 3, x: 0.66, y: 0.17 },
  { id: 4, x: 1.0, y: 0.22 }
];

const DEFAULT_FEATURES = {
  parallax: true,
  sun: true,
  moon: true,
  stars: true,
  atmosphere: true,
  lensFlare: true,
  clouds: true,
  lowPowerThrottle: true
};

const READY_WALLPAPER_PRESETS = [
  {
    id: 'warrior',
    name: 'Warrior Fantasy HD',
    layers: DEFAULT_LAYERS
  }
];

const normalizeLayerOrder = (list) => list.map((layer) => {
  const normalizedLayer = { visible: true, ...layer };
  delete normalizedLayer.zIndex;
  return normalizedLayer;
});

const formatTime = (time) => {
  const hours = Math.floor(time);
  const minutes = Math.floor((time - hours) * 60);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
};

const SNAPSHOT_TIMES = [
  { label: 'Sunrise', value: 6.5 },
  { label: 'Noon', value: 12 },
  { label: 'Sunset', value: 18.5 },
  { label: 'Night', value: 22 }
];

const mapMotionPathToAndroid = (motionType) => (motionType === 'ARCH' ? 'ARC' : 'VERTICAL');

const averageHorizonOffset = (points) => {
  if (!points.length) return 0.2;
  const total = points.reduce((sum, point) => sum + point.y, 0);
  return parseFloat((total / points.length).toFixed(4));
};

const sortedLayerPaths = (layers) =>
  layers.map((layer) => layer.texturePath);

const progressFromDuration = (time, duration) => {
  const safeDuration = Math.max(0.1, duration || 5);
  return (time % safeDuration) / safeDuration;
};

const hexToRgb = (hex) => {
  const cleanHex = hex.replace('#', '');
  const bigint = parseInt(cleanHex, 16);
  const r = ((bigint >> 16) & 255) / 255;
  const g = ((bigint >> 8) & 255) / 255;
  const b = (bigint & 255) / 255;
  return [r, g, b];
};

function App() {
  const [wallpaperId, setWallpaperId] = useState('creator_draft');
  const [wallpaperName, setWallpaperName] = useState('Creator Draft HD');
  const [layers, setLayers] = useState(DEFAULT_LAYERS);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [daylightTime, setDaylightTime] = useState(12); // 0 to 24 hours
  const [workspaceScale, setWorkspaceScale] = useState(1.35);
  const [showSafeFrame, setShowSafeFrame] = useState(true);
  
  // Parallax state
  const [featureFlags, setFeatureFlags] = useState(DEFAULT_FEATURES);
  const enableParallax = featureFlags.parallax;
  const [parallaxX, setParallaxX] = useState(0);
  const [parallaxY, setParallaxY] = useState(0);
  const [timeState, setTimeState] = useState(0);

  // GLSL Shader properties
  const [glslCode, setGlslCode] = useState(DEFAULT_GLSL_SHADER);
  const [shaderError, setShaderError] = useState(null);

  // Sun configurations
  const [sunSize, setSunSize] = useState(0.06);
  const [sunColor, setSunColor] = useState('#ffd54f');
  const [sunZenith, setSunZenith] = useState(0.82);
  const [sunMotionType, setSunMotionType] = useState('ARCH'); // ARCH, LINEAR, STATIC
  const [sunStaticX, setSunStaticX] = useState(0.5);
  const [sunStaticY, setSunStaticY] = useState(0.16);
  const [sunGlow, setSunGlow] = useState(0.4);

  // Moon configurations
  const [moonSize, setMoonSize] = useState(0.05);
  const [moonColor, setMoonColor] = useState('#eceff1');
  const [moonZenith, setMoonZenith] = useState(0.78);
  const [moonMotionType, setMoonMotionType] = useState('ARCH'); // ARCH, LINEAR, STATIC
  const [moonStaticX, setMoonStaticX] = useState(0.3);
  const [moonStaticY, setMoonStaticY] = useState(0.14);
  const [moonGlow, setMoonGlow] = useState(0.25);

  // Extended sky parameters
  const starsEnabled = featureFlags.stars;
  const [starsDensity, setStarsDensity] = useState(0.5);
  const [atmosphereIntensity, setAtmosphereIntensity] = useState(1.0);
  const [cloudSpeed, setCloudSpeed] = useState(0.28);
  const [cloudDensity, setCloudDensity] = useState(0.46);
  const [cloudIntensity, setCloudIntensity] = useState(0.3);

  // Skyline Multi-Point Horizon
  const [horizonPoints, setHorizonPoints] = useState(DEFAULT_HORIZON);
  const [editingHorizonId, setEditingHorizonId] = useState(null);

  // Dialog Modals
  const [showExportModal, setShowExportModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [exportMode, setExportMode] = useState('manifest');
  const [importText, setImportText] = useState('');

  // Refs
  const canvasRef = useRef(null);
  const addLayerFileRef = useRef(null);
  const phoneScreenRef = useRef(null);
  const webglProgramRef = useRef(null);
  const webglBufferRef = useRef(null);
  const horizonTextureRef = useRef(null);
  const animationFrameRef = useRef(null);
  const renderFrameRef = useRef(null);
  const renderStateRef = useRef({});
  const timeStateRef = useRef(0);
  const localUrlsRef = useRef([]);
  const startTimeRef = useRef(null);

  const activeLayer = layers[selectedIndex];
  const effectiveAtmosphereIntensity = featureFlags.atmosphere ? atmosphereIntensity : 0;
  const effectiveSunSize = featureFlags.sun ? sunSize : 0;
  const effectiveMoonSize = featureFlags.moon ? moonSize : 0;

  // Render order follows the layer list order.
  const sortedLayers = useMemo(() => {
    return layers.map((layer, index) => ({ layer, index }));
  }, [layers]);

  const sceneStackItems = useMemo(() => {
    const systemItems = [
      { key: 'system-atmosphere', label: 'Atmosphere', detail: 'Sky shader', type: 'system', featureKey: 'atmosphere', visible: featureFlags.atmosphere },
      { key: 'system-stars', label: 'Stars', detail: `Density ${starsDensity.toFixed(2)}`, type: 'system', featureKey: 'stars', visible: featureFlags.stars },
      { key: 'system-clouds', label: 'Clouds', detail: `Speed ${cloudSpeed.toFixed(2)}`, type: 'system', featureKey: 'clouds', visible: featureFlags.clouds },
      { key: 'system-sun', label: 'Sun', detail: sunMotionType, type: 'system', featureKey: 'sun', visible: featureFlags.sun },
      { key: 'system-moon', label: 'Moon', detail: moonMotionType, type: 'system', featureKey: 'moon', visible: featureFlags.moon }
    ];

    return [
      ...systemItems,
      ...layers.map((layer, index) => ({
        key: `texture-${index}`,
        label: layer.texturePath.split('/').pop() || `Layer ${index}`,
        detail: layer.motionType,
        type: 'texture',
        visible: layer.visible !== false,
        textureIndex: index
      }))
    ];
  }, [layers, featureFlags, starsDensity, cloudSpeed, sunMotionType, moonMotionType]);

  // Layer editing dispatcher
  const updateActiveLayer = (field, val) => {
    setLayers(prev => prev.map((l, i) => {
      if (i === selectedIndex) {
        return { ...l, [field]: val };
      }
      return l;
    }));
  };

  const updateLayerAtIndex = (index, patch) => {
    setLayers(prev => prev.map((layer, layerIndex) => (
      layerIndex === index ? { ...layer, ...patch } : layer
    )));
  };

  const moveLayerToIndex = (fromIndex, toIndex) => {
    const targetIndex = Math.max(0, Math.min(layers.length - 1, toIndex));
    setLayers(prev => {
      const list = [...prev];
      const [item] = list.splice(fromIndex, 1);
      list.splice(targetIndex, 0, item);
      return normalizeLayerOrder(list);
    });
    setSelectedIndex(targetIndex);
  };

  const loadReadyPreset = (presetId) => {
    const preset = READY_WALLPAPER_PRESETS.find((item) => item.id === presetId);
    if (!preset) return;
    setWallpaperId(preset.id);
    setWallpaperName(preset.name);
    setLayers(normalizeLayerOrder(preset.layers.map((layer) => ({ ...layer, localUrl: null }))));
    setSelectedIndex(0);
  };

  const updateFeatureFlag = (key, value) => {
    setFeatureFlags(prev => ({ ...prev, [key]: value }));
  };

  useEffect(() => {
    localUrlsRef.current = layers.map((layer) => layer.localUrl).filter(Boolean);
  }, [layers]);

  useEffect(() => {
    return () => {
      localUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
    };
  }, []);

  const clearActiveLayerPreview = () => {
    if (activeLayer?.localUrl) {
      URL.revokeObjectURL(activeLayer.localUrl);
    }
    updateActiveLayer('localUrl', null);
  };

  // File replacement inside properties editor
  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file && activeLayer) {
      if (activeLayer.localUrl) {
        URL.revokeObjectURL(activeLayer.localUrl);
      }
      const url = URL.createObjectURL(file);
      updateActiveLayer('localUrl', url);
      updateActiveLayer('texturePath', `wallpapers/custom/${file.name}`);
    }
    e.target.value = null;
  };

  // 12-Second Daylight Cycle Loop & Animation Timer
  useEffect(() => {
    timeStateRef.current = timeState;
  }, [timeState]);

  useEffect(() => {
    startTimeRef.current = Date.now() - (timeStateRef.current * 1000);
    
    const animate = () => {
      const elapsed = (Date.now() - startTimeRef.current) / 1000;
      setTimeState(elapsed);
      
      if (isPlaying) {
        // Full daylight cycle loop in 12 seconds
        const newDaylight = (elapsed / 12 * 24) % 24;
        setDaylightTime(newDaylight);
      }
      
      animationFrameRef.current = requestAnimationFrame(animate);
    };
    
    animationFrameRef.current = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(animationFrameRef.current);
  }, [isPlaying]);

  // Night Blend calculation
  const nightBlend = useMemo(() => {
    if (daylightTime >= 6 && daylightTime < 7) {
      return 1 - (daylightTime - 6);
    } else if (daylightTime >= 7 && daylightTime < 17) {
      return 0;
    } else if (daylightTime >= 17 && daylightTime < 19) {
      return (daylightTime - 17) / 2;
    } else {
      return 1;
    }
  }, [daylightTime]);

  // Interpolate Sun Position Y vertically on LINEAR
  const sunPosition = useMemo(() => {
    if (sunMotionType === 'STATIC') {
      return [sunStaticX, sunStaticY];
    }
    if (daylightTime < 6 || daylightTime >= 18) {
      return [sunStaticX, -0.2];
    }
    const progress = (daylightTime - 6) / 12;
    const arcY = sunStaticY + (sunZenith - sunStaticY) * Math.sin(Math.PI * progress);
    
    if (sunMotionType === 'LINEAR') {
      return [sunStaticX, arcY];
    }
    // ARCH
    const sX = sunStaticX + (progress - 0.5) * 0.76;
    return [sX, arcY];
  }, [daylightTime, sunMotionType, sunStaticX, sunStaticY, sunZenith]);

  // Interpolate Moon Position Y vertically on LINEAR
  const moonPosition = useMemo(() => {
    if (moonMotionType === 'STATIC') {
      return [moonStaticX, moonStaticY];
    }
    if (daylightTime >= 6 && daylightTime < 18) {
      return [moonStaticX, -0.2];
    }
    const nightTime = daylightTime >= 18 ? daylightTime : daylightTime + 24;
    const progress = (nightTime - 18) / 12;
    const arcY = moonStaticY + (moonZenith - moonStaticY) * Math.sin(Math.PI * progress);

    if (moonMotionType === 'LINEAR') {
      return [moonStaticX, arcY];
    }
    // ARCH
    const mX = moonStaticX + (progress - 0.5) * 0.76;
    return [mX, arcY];
  }, [daylightTime, moonMotionType, moonStaticX, moonStaticY, moonZenith]);

  // Parallax Device Orientation Handlers
  useEffect(() => {
    if (!enableParallax) {
      return;
    }

    const handleOrientation = (e) => {
      const { beta, gamma } = e;
      if (beta === null || gamma === null) return;
      
      let x = gamma / 45;
      let y = (beta - 65) / 30;
      
      x = Math.max(-1, Math.min(1, x));
      y = Math.max(-1, Math.min(1, y));
      
      setParallaxX(x);
      setParallaxY(y);
    };

    window.addEventListener('deviceorientation', handleOrientation);
    return () => window.removeEventListener('deviceorientation', handleOrientation);
  }, [enableParallax]);

  // Request Mobile Gyro Permission
  const handleParallaxCheckboxChange = async (e) => {
    const checked = e.target.checked;
    updateFeatureFlag('parallax', checked);
    if (!checked) {
      setParallaxX(0);
      setParallaxY(0);
      return;
    }
    
    if (checked && typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
      try {
        const permission = await DeviceOrientationEvent.requestPermission();
        if (permission !== 'granted') {
          alert('Device Orientation permission was denied. Falling back to mouse controls.');
        }
      } catch (err) {
        console.warn('Gyro permissions request failed:', err);
      }
    }
  };

  // Mouse tilt controller on desktop
  const handleMouseMove = (e) => {
    if (!enableParallax || !phoneScreenRef.current) return;
    if (window.matchMedia("(pointer: coarse)").matches) return;

    const rect = phoneScreenRef.current.getBoundingClientRect();
    const midX = rect.left + rect.width / 2;
    const midY = rect.top + rect.height / 2;
    
    const x = (e.clientX - midX) / (rect.width / 2);
    const y = (e.clientY - midY) / (rect.height / 2);
    
    setParallaxX(Math.max(-1, Math.min(1, x)));
    setParallaxY(Math.max(-1, Math.min(1, -y)));
  };

  const handleMouseLeave = () => {
    if (enableParallax) {
      setParallaxX(0);
      setParallaxY(0);
    }
  };

  // WebGL context setup and compiler
  const compileWebGLShaders = (gl, sourceCode) => {
    const vsSource = `
      attribute vec2 position;
      varying vec2 v_TexCoord;
      void main() {
        v_TexCoord = position * 0.5 + 0.5;
        gl_Position = vec4(position, 0.0, 1.0);
      }
    `;

    try {
      const compileShader = (glContext, source, type) => {
        const shader = glContext.createShader(type);
        glContext.shaderSource(shader, source);
        glContext.compileShader(shader);
        if (!glContext.getShaderParameter(shader, glContext.COMPILE_STATUS)) {
          const info = glContext.getShaderInfoLog(shader);
          glContext.deleteShader(shader);
          throw new Error(info);
        }
        return shader;
      };

      const vs = compileShader(gl, vsSource, gl.VERTEX_SHADER);
      const fs = compileShader(gl, sourceCode, gl.FRAGMENT_SHADER);
      
      const program = gl.createProgram();
      gl.attachShader(program, vs);
      gl.attachShader(program, fs);
      gl.linkProgram(program);
      
      if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
        const info = gl.getProgramInfoLog(program);
        gl.deleteProgram(program);
        throw new Error(info);
      }

      webglProgramRef.current = program;
      setShaderError(null);
    } catch (err) {
      console.error('WebGL Shader Compilation Error:', err.message);
      setShaderError(err.message);
    }
  };

  // Initialize WebGL buffers and compile shader on startup
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const gl = canvas.getContext('webgl');
    if (!gl) {
      setShaderError('WebGL not supported by your browser');
      return;
    }

    compileWebGLShaders(gl, glslCode);

    // Setup vertices
    const vertices = new Float32Array([
      -1, -1,
      -1,  1,
       1, -1,
       1,  1
    ]);
    const buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, vertices, gl.STATIC_DRAW);
    webglBufferRef.current = buffer;

    // Create 1D data texture for Horizon Points skyline mapping
    const texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    horizonTextureRef.current = texture;
  }, [glslCode]);

  // Interpolate and upload horizon heights to WebGL texture
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const gl = canvas.getContext('webgl');
    if (!gl || !horizonTextureRef.current) return;

    // Sort horizon points by X coordinate
    const sortedPoints = [...horizonPoints].sort((a, b) => a.x - b.x);

    // Helper to evaluate height at coordinate X
    const getHorizonHeightAt = (x) => {
      if (sortedPoints.length === 0) return 0.2;
      if (x <= sortedPoints[0].x) return sortedPoints[0].y;
      if (x >= sortedPoints[sortedPoints.length - 1].x) return sortedPoints[sortedPoints.length - 1].y;
      
      for (let i = 0; i < sortedPoints.length - 1; i++) {
        const pA = sortedPoints[i];
        const pB = sortedPoints[i + 1];
        if (x >= pA.x && x <= pB.x) {
          const t = (x - pA.x) / (pB.x - pA.x);
          const smoothT = t * t * (3 - 2 * t); // smoothstep lerp
          return pA.y + (pB.y - pA.y) * smoothT;
        }
      }
      return 0.2;
    };

    // Build 256 pixel height array
    const data = new Uint8Array(256);
    for (let i = 0; i < 256; i++) {
      const x = i / 255;
      const y = getHorizonHeightAt(x);
      data[i] = Math.floor(Math.max(0, Math.min(1.0, y)) * 255);
    }

    gl.bindTexture(gl.TEXTURE_2D, horizonTextureRef.current);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.LUMINANCE, 256, 1, 0, gl.LUMINANCE, gl.UNSIGNED_BYTE, data);
  }, [horizonPoints]);

  useEffect(() => {
    renderStateRef.current = {
      timeState,
      daylightTime,
      sunPosition,
      moonPosition,
      sunColor,
      moonColor,
      sunSize: effectiveSunSize,
      moonSize: effectiveMoonSize,
      nightBlend,
      sunGlow,
      moonGlow,
      starsEnabled,
      starsDensity,
      atmosphereIntensity: effectiveAtmosphereIntensity
    };
  }, [timeState, daylightTime, sunPosition, moonPosition, sunColor, moonColor, effectiveSunSize, effectiveMoonSize, nightBlend, sunGlow, moonGlow, starsEnabled, starsDensity, effectiveAtmosphereIntensity]);

  // WebGL Render Loop
  useEffect(() => {
    let active = true;
    
    const draw = () => {
      if (!active) return;
      const state = renderStateRef.current;
      if (!state.sunPosition || !state.moonPosition) {
        renderFrameRef.current = requestAnimationFrame(draw);
        return;
      }
      
      const canvas = canvasRef.current;
      if (canvas) {
        const gl = canvas.getContext('webgl');
        const program = webglProgramRef.current;
        
        if (gl && program && webglBufferRef.current && horizonTextureRef.current) {
          gl.viewport(0, 0, canvas.width, canvas.height);
          gl.clear(gl.COLOR_BUFFER_BIT);
          gl.useProgram(program);

          // Bind vertices
          const posAttrib = gl.getAttribLocation(program, 'position');
          gl.enableVertexAttribArray(posAttrib);
          gl.bindBuffer(gl.ARRAY_BUFFER, webglBufferRef.current);
          gl.vertexAttribPointer(posAttrib, 2, gl.FLOAT, false, 0, 0);

          // Bind Horizon height texture mapping
          gl.activeTexture(gl.TEXTURE0);
          gl.bindTexture(gl.TEXTURE_2D, horizonTextureRef.current);
          gl.uniform1i(gl.getUniformLocation(program, 'u_HorizonTexture'), 0);

          // Bind Uniform variables
          gl.uniform2f(gl.getUniformLocation(program, 'u_Resolution'), canvas.width, canvas.height);
          gl.uniform1f(gl.getUniformLocation(program, 'u_Time'), state.timeState);
          gl.uniform1f(gl.getUniformLocation(program, 'u_TimeOfDay'), state.daylightTime);
          gl.uniform2f(gl.getUniformLocation(program, 'u_SunPos'), state.sunPosition[0], state.sunPosition[1]);
          gl.uniform2f(gl.getUniformLocation(program, 'u_MoonPos'), state.moonPosition[0], state.moonPosition[1]);
          
          const sunRgb = hexToRgb(state.sunColor);
          const moonRgb = hexToRgb(state.moonColor);
          gl.uniform3f(gl.getUniformLocation(program, 'u_SunColor'), sunRgb[0], sunRgb[1], sunRgb[2]);
          gl.uniform3f(gl.getUniformLocation(program, 'u_MoonColor'), moonRgb[0], moonRgb[1], moonRgb[2]);
          
          gl.uniform1f(gl.getUniformLocation(program, 'u_SunSize'), state.sunSize);
          gl.uniform1f(gl.getUniformLocation(program, 'u_MoonSize'), state.moonSize);
          gl.uniform1f(gl.getUniformLocation(program, 'u_NightBlend'), state.nightBlend);

          // Bind extended parameters uniforms
          gl.uniform1f(gl.getUniformLocation(program, 'u_SunGlow'), state.sunGlow);
          gl.uniform1f(gl.getUniformLocation(program, 'u_MoonGlow'), state.moonGlow);
          gl.uniform1f(gl.getUniformLocation(program, 'u_StarsEnabled'), state.starsEnabled ? 1.0 : 0.0);
          gl.uniform1f(gl.getUniformLocation(program, 'u_StarsDensity'), state.starsDensity);
          gl.uniform1f(gl.getUniformLocation(program, 'u_AtmosphereIntensity'), state.atmosphereIntensity);

          gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        }
      }
      renderFrameRef.current = requestAnimationFrame(draw);
    };

    draw();
    return () => {
      active = false;
      if (renderFrameRef.current) {
        cancelAnimationFrame(renderFrameRef.current);
      }
    };
  }, []);

  // Drag and Drop positioning (Mapped to cover size coordinates)
  const handleDragStart = (e, layerIndex = selectedIndex) => {
    const targetLayer = layers[layerIndex];
    if (!targetLayer) return;
    e.stopPropagation();
    e.preventDefault();
    setSelectedIndex(layerIndex);

    const isTouch = e.type === 'touchstart';
    const clientX = isTouch ? e.touches[0].clientX : e.clientX;
    const clientY = isTouch ? e.touches[0].clientY : e.clientY;

    const startX = clientX;
    const startY = clientY;
    const startOffsetX = targetLayer.offsetX;
    const startOffsetY = targetLayer.offsetY;

    const handleDragMove = (moveEvent) => {
      moveEvent.preventDefault();
      const currentIsTouch = moveEvent.type === 'touchmove';
      const currentX = currentIsTouch ? moveEvent.touches[0].clientX : moveEvent.clientX;
      const currentY = currentIsTouch ? moveEvent.touches[0].clientY : moveEvent.clientY;

      const deltaX = currentX - startX;
      const deltaY = currentY - startY;

      // Cover sizing translation scale mapping: 340px width, 620px height
      const newOffsetX = startOffsetX + (deltaX / 340);
      const newOffsetY = startOffsetY - (deltaY / 620);

      updateLayerAtIndex(layerIndex, {
        offsetX: parseFloat(Math.max(-2.5, Math.min(2.5, newOffsetX)).toFixed(4)),
        offsetY: parseFloat(Math.max(-2.5, Math.min(2.5, newOffsetY)).toFixed(4))
      });
    };

    const handleDragEnd = () => {
      window.removeEventListener('mousemove', handleDragMove);
      window.removeEventListener('mouseup', handleDragEnd);
      window.removeEventListener('touchmove', handleDragMove);
      window.removeEventListener('touchend', handleDragEnd);
    };

    window.addEventListener('mousemove', handleDragMove);
    window.addEventListener('mouseup', handleDragEnd);
    window.addEventListener('touchmove', handleDragMove, { passive: false });
    window.addEventListener('touchend', handleDragEnd);
  };

  const handleResizeStart = (e, layerIndex) => {
    const targetLayer = layers[layerIndex];
    if (!targetLayer) return;
    e.stopPropagation();
    e.preventDefault();
    setSelectedIndex(layerIndex);

    const isTouch = e.type === 'touchstart';
    const clientX = isTouch ? e.touches[0].clientX : e.clientX;
    const clientY = isTouch ? e.touches[0].clientY : e.clientY;
    const startX = clientX;
    const startY = clientY;
    const startScaleX = targetLayer.scaleX;
    const startScaleY = targetLayer.scaleY;

    const handleResizeMove = (moveEvent) => {
      moveEvent.preventDefault();
      const currentIsTouch = moveEvent.type === 'touchmove';
      const currentX = currentIsTouch ? moveEvent.touches[0].clientX : moveEvent.clientX;
      const currentY = currentIsTouch ? moveEvent.touches[0].clientY : moveEvent.clientY;
      const delta = ((currentX - startX) + (currentY - startY)) / 360;
      updateLayerAtIndex(layerIndex, {
        scaleX: parseFloat(Math.max(0.1, Math.min(4, startScaleX + delta)).toFixed(3)),
        scaleY: parseFloat(Math.max(0.1, Math.min(4, startScaleY + delta)).toFixed(3))
      });
    };

    const handleResizeEnd = () => {
      window.removeEventListener('mousemove', handleResizeMove);
      window.removeEventListener('mouseup', handleResizeEnd);
      window.removeEventListener('touchmove', handleResizeMove);
      window.removeEventListener('touchend', handleResizeEnd);
    };

    window.addEventListener('mousemove', handleResizeMove);
    window.addEventListener('mouseup', handleResizeEnd);
    window.addEventListener('touchmove', handleResizeMove, { passive: false });
    window.addEventListener('touchend', handleResizeEnd);
  };

  // Add Layer with File dialog trigger
  const triggerAddLayerFile = () => {
    if (layers.length >= 15) {
      alert('Maximum limit of 15 layers reached!');
      return;
    }
    if (addLayerFileRef.current) {
      addLayerFileRef.current.click();
    }
  };

  const handleAddLayerFile = (e) => {
    const file = e.target.files[0];
    if (file) {
      const url = URL.createObjectURL(file);
      const newLayer = {
        texturePath: `wallpapers/custom/${file.name}`,
        offsetX: 0,
        offsetY: 0,
        scaleX: 1.0,
        scaleY: 1.0,
        motionType: 'STATIC',
        motionSpeed: 1,
        motionAmplitude: 0,
        motionDirection: 0,
        motionDuration: 5,
        motionStartX: 0,
        motionStartY: 0,
        motionEndX: 0.25,
        motionEndY: 0,
        parallaxStrengthX: 0.05,
        parallaxStrengthY: 0.03,
        nightTintFactor: 0.5,
        opacity: 1.0,
        visible: true,
        localUrl: url
      };
      setLayers(normalizeLayerOrder([...layers, newLayer]));
      setSelectedIndex(layers.length);
    }
    e.target.value = null;
  };

  // Swapping indices
  const moveLayerUp = (index, e) => {
    e.stopPropagation();
    if (index === 0) return;
    moveLayerToIndex(index, index - 1);
  };

  const moveLayerDown = (index, e) => {
    e.stopPropagation();
    if (index === layers.length - 1) return;
    moveLayerToIndex(index, index + 1);
  };

  const deleteLayer = (idx) => {
    if (layers.length <= 1) return;
    if (layers[idx]?.localUrl) {
      URL.revokeObjectURL(layers[idx].localUrl);
    }
    setLayers(prev => normalizeLayerOrder(prev.filter((_, i) => i !== idx)));
    setSelectedIndex(0);
  };

  // Horizon point managers
  const addHorizonPoint = () => {
    if (horizonPoints.length >= 16) {
      alert('Maximum limit of 16 horizon points reached!');
      return;
    }
    const newPoint = {
      id: Date.now(),
      x: 0.5,
      y: 0.2
    };
    setHorizonPoints([...horizonPoints, newPoint]);
    setEditingHorizonId(newPoint.id);
  };

  const deleteHorizonPoint = (id) => {
    if (horizonPoints.length <= 2) {
      alert('A minimum of 2 points is required to form a line.');
      return;
    }
    setHorizonPoints(prev => prev.filter(p => p.id !== id));
    if (editingHorizonId === id) setEditingHorizonId(null);
  };

  const updateHorizonPoint = (id, field, value) => {
    setHorizonPoints(prev => prev.map(p => {
      if (p.id === id) {
        return { ...p, [field]: value };
      }
      return p;
    }));
  };

  const sortedHorizonPoints = useMemo(() => {
    return [...horizonPoints].sort((a, b) => a.x - b.x);
  }, [horizonPoints]);

  const generatedLayoutJson = useMemo(() => {
    const outputLayers = layers.map((l, index) => ({
      texturePath: l.texturePath,
      zIndex: index,
      offsetX: parseFloat(l.offsetX.toFixed(4)),
      offsetY: parseFloat(l.offsetY.toFixed(4)),
      scaleX: parseFloat(l.scaleX.toFixed(4)),
      scaleY: parseFloat(l.scaleY.toFixed(4)),
      motionType: l.motionType,
      motionSpeed: parseFloat(l.motionSpeed.toFixed(2)),
      motionAmplitude: parseFloat(l.motionAmplitude.toFixed(4)),
      motionDirection: l.motionDirection || 0,
      motionDuration: parseFloat((l.motionDuration ?? 5).toFixed(2)),
      motionStartX: parseFloat((l.motionStartX ?? 0).toFixed(4)),
      motionStartY: parseFloat((l.motionStartY ?? 0).toFixed(4)),
      motionEndX: parseFloat((l.motionEndX ?? 0).toFixed(4)),
      motionEndY: parseFloat((l.motionEndY ?? 0).toFixed(4)),
      parallaxStrengthX: parseFloat((l.parallaxStrengthX ?? 0.05).toFixed(4)),
      parallaxStrengthY: parseFloat((l.parallaxStrengthY ?? 0.03).toFixed(4)),
      nightTintFactor: parseFloat(l.nightTintFactor.toFixed(4)),
      opacity: parseFloat((l.opacity ?? 1.0).toFixed(4)),
      visible: l.visible !== false
    }));

    const outputHorizon = sortedHorizonPoints.map(p => ({
      x: parseFloat(p.x.toFixed(4)),
      y: parseFloat(p.y.toFixed(4))
    }));

    return JSON.stringify({
      meta: {
        id: wallpaperId,
        name: wallpaperName,
        type: 'creator_draft'
      },
      layers: outputLayers,
      skyShader: {
        glslCode: glslCode,
        sun: {
          size: sunSize,
          color: sunColor,
          zenith: sunZenith,
          motionType: sunMotionType,
          staticX: sunStaticX,
          staticY: sunStaticY,
          glow: sunGlow
        },
        moon: {
          size: moonSize,
          color: moonColor,
          zenith: moonZenith,
          motionType: moonMotionType,
          staticX: moonStaticX,
          staticY: moonStaticY,
          glow: moonGlow
        },
        horizonPoints: outputHorizon,
        features: featureFlags,
        starsEnabled: starsEnabled,
        starsDensity: starsDensity,
        atmosphereIntensity: atmosphereIntensity,
        clouds: {
          speed: cloudSpeed,
          density: cloudDensity,
          intensity: cloudIntensity
        }
      }
    }, null, 2);
  }, [wallpaperId, wallpaperName, layers, glslCode, sunSize, sunColor, sunZenith, sunMotionType, sunStaticX, sunStaticY, sunGlow, moonSize, moonColor, moonZenith, moonMotionType, moonStaticX, moonStaticY, moonGlow, sortedHorizonPoints, featureFlags, starsEnabled, starsDensity, atmosphereIntensity, cloudSpeed, cloudDensity, cloudIntensity]);

  const generatedManifestJson = useMemo(() => {
    const layerPaths = sortedLayerPaths(layers);
    const hasMotion = layers.some((layer) => layer.motionType !== 'STATIC');
    const intervalMs = hasMotion ? 16 : 100;

    return JSON.stringify({
      id: wallpaperId.trim() || 'creator_draft',
      name: wallpaperName.trim() || 'Creator Draft HD',
      horizon: {
        offset: averageHorizonOffset(horizonPoints)
      },
      textures: {
        backgroundTexture: layerPaths[0] ?? null,
        flareTexture: layerPaths[1] ?? null,
        moonTexture: layerPaths[2] ?? null
      },
      features: {
        atmosphereEnabled: featureFlags.atmosphere,
        lensFlareEnabled: featureFlags.lensFlare,
        starsEnabled: featureFlags.stars
      },
      effects: {
        clouds: {
          enabled: featureFlags.clouds,
          speed: parseFloat(cloudSpeed.toFixed(2)),
          density: parseFloat(cloudDensity.toFixed(2)),
          intensity: parseFloat(cloudIntensity.toFixed(2))
        },
        stars: {
          enabled: featureFlags.stars,
          density: parseFloat(starsDensity.toFixed(2)),
          intensity: parseFloat(Math.min(1, atmosphereIntensity).toFixed(2))
        }
      },
      celestial: {
        sunPathType: mapMotionPathToAndroid(sunMotionType),
        moonPathType: mapMotionPathToAndroid(moonMotionType),
        sunOrbit: {
          pathType: mapMotionPathToAndroid(sunMotionType),
          startX: parseFloat(sunStaticX.toFixed(4)),
          endX: 0.5,
          peakY: parseFloat(sunZenith.toFixed(4)),
          curve: 'LINEAR'
        },
        moonOrbit: {
          pathType: mapMotionPathToAndroid(moonMotionType),
          startX: parseFloat(moonStaticX.toFixed(4)),
          endX: 0.5,
          peakY: parseFloat(moonZenith.toFixed(4)),
          curve: 'LINEAR'
        }
      },
      shader: {
        fragmentAssetPath: `shaders/${wallpaperId.trim() || 'creator_draft'}/fragment.glsl`,
        mode: 'external_theme',
        uniformOverrides: {}
      },
      previewLoopDurationSeconds: 12.0,
      runtimeRenderPolicy: {
        policy: hasMotion ? 'CONTINUOUS' : 'MINUTE_TICK',
        continuousFrameIntervalMs: intervalMs
      },
      capabilities: {
        dynamicMotion: hasMotion,
        dynamicTextures: false,
        locationAwareLighting: true,
        supportsCloudLayer: featureFlags.clouds,
        supportsStarLayer: featureFlags.stars
      },
      serviceRenderPolicy: {
        overridePolicy: hasMotion ? 'CONTINUOUS' : 'MINUTE_TICK',
        overrideFrameIntervalMs: intervalMs,
        usePowerSaverThrottle: featureFlags.lowPowerThrottle,
        useThermalThrottle: featureFlags.lowPowerThrottle
      }
    }, null, 2);
  }, [wallpaperId, wallpaperName, layers, horizonPoints, atmosphereIntensity, starsDensity, sunMotionType, moonMotionType, sunStaticX, moonStaticX, sunZenith, moonZenith, featureFlags, cloudSpeed, cloudDensity, cloudIntensity]);

  const generatedJson = exportMode === 'manifest' ? generatedManifestJson : generatedLayoutJson;
  const exportModeLabel = exportMode === 'manifest' ? 'Lumisky Manifest' : 'Creator Draft';

  const handleImport = () => {
    try {
      const parsed = JSON.parse(importText);
      if (parsed.meta?.id || parsed.id) setWallpaperId(parsed.meta?.id ?? parsed.id);
      if (parsed.meta?.name || parsed.name) setWallpaperName(parsed.meta?.name ?? parsed.name);

      const manifestTexturePaths = parsed.textures ? [
        parsed.textures.backgroundTexture,
        parsed.textures.flareTexture,
        parsed.textures.moonTexture
      ].filter(Boolean).map((texturePath) => ({ texturePath })) : null;
      const importedLayers = parsed.layers || (Array.isArray(parsed) ? parsed : manifestTexturePaths);
      if (!importedLayers) throw new Error('Invalid structure');

      const sanitizedLayers = importedLayers.map((l) => ({
        texturePath: l.texturePath || 'warrior/warrior1.webp',
        offsetX: typeof l.offsetX === 'number' ? l.offsetX : 0,
        offsetY: typeof l.offsetY === 'number' ? l.offsetY : 0,
        scaleX: typeof l.scaleX === 'number' ? l.scaleX : 1,
        scaleY: typeof l.scaleY === 'number' ? l.scaleY : 1,
        motionType: l.motionType || 'STATIC',
        motionSpeed: typeof l.motionSpeed === 'number' ? l.motionSpeed : 1,
        motionAmplitude: typeof l.motionAmplitude === 'number' ? l.motionAmplitude : 0,
        motionDirection: typeof l.motionDirection === 'number' ? l.motionDirection : 0,
        motionDuration: typeof l.motionDuration === 'number' ? l.motionDuration : 5,
        motionStartX: typeof l.motionStartX === 'number' ? l.motionStartX : 0,
        motionStartY: typeof l.motionStartY === 'number' ? l.motionStartY : 0,
        motionEndX: typeof l.motionEndX === 'number' ? l.motionEndX : 0.25,
        motionEndY: typeof l.motionEndY === 'number' ? l.motionEndY : 0,
        parallaxStrengthX: typeof l.parallaxStrengthX === 'number' ? l.parallaxStrengthX : 0.05,
        parallaxStrengthY: typeof l.parallaxStrengthY === 'number' ? l.parallaxStrengthY : 0.03,
        nightTintFactor: typeof l.nightTintFactor === 'number' ? l.nightTintFactor : 0.5,
        opacity: typeof l.opacity === 'number' ? l.opacity : 1.0,
        visible: l.visible !== false,
        localUrl: null
      }));

      setLayers(normalizeLayerOrder(sanitizedLayers.slice(0, 15)));
      setSelectedIndex(0);

      if (parsed.horizon?.offset !== undefined) {
        const offset = Number(parsed.horizon.offset);
        if (!Number.isNaN(offset)) {
          setHorizonPoints([
            { id: 1, x: 0.0, y: offset },
            { id: 2, x: 1.0, y: offset }
          ]);
        }
      }

      if (parsed.features) {
        setFeatureFlags(prev => ({
          ...prev,
          atmosphere: parsed.features.atmosphereEnabled ?? prev.atmosphere,
          lensFlare: parsed.features.lensFlareEnabled ?? prev.lensFlare,
          stars: parsed.features.starsEnabled ?? prev.stars
        }));
        setAtmosphereIntensity(parsed.features.atmosphereEnabled === false ? 0 : 1.0);
      }

      if (parsed.effects?.clouds) {
        setFeatureFlags(prev => ({ ...prev, clouds: parsed.effects.clouds.enabled ?? prev.clouds }));
        setCloudSpeed(parsed.effects.clouds.speed ?? 0.28);
        setCloudDensity(parsed.effects.clouds.density ?? 0.46);
        setCloudIntensity(parsed.effects.clouds.intensity ?? 0.3);
      }

      if (parsed.celestial) {
        setSunMotionType(parsed.celestial.sunPathType === 'ARC' ? 'ARCH' : 'LINEAR');
        setMoonMotionType(parsed.celestial.moonPathType === 'ARC' ? 'ARCH' : 'LINEAR');
      }

      // Load sky configurations if present
      if (parsed.skyShader) {
        const s = parsed.skyShader;
        if (s.glslCode) setGlslCode(s.glslCode);
        if (s.sun) {
          setSunSize(s.sun.size ?? 0.06);
          setSunColor(s.sun.color ?? '#ffd54f');
          setSunZenith(s.sun.zenith ?? 0.82);
          setSunMotionType(s.sun.motionType ?? 'ARCH');
          setSunStaticX(s.sun.staticX ?? 0.5);
          setSunStaticY(s.sun.staticY ?? 0.16);
          setSunGlow(s.sun.glow ?? 0.4);
        }
        if (s.moon) {
          setMoonSize(s.moon.size ?? 0.05);
          setMoonColor(s.moon.color ?? '#eceff1');
          setMoonZenith(s.moon.zenith ?? 0.78);
          setMoonMotionType(s.moon.motionType ?? 'ARCH');
          setMoonStaticX(s.moon.staticX ?? 0.3);
          setMoonStaticY(s.moon.staticY ?? 0.14);
          setMoonGlow(s.moon.glow ?? 0.25);
        }
        if (s.horizonPoints && Array.isArray(s.horizonPoints)) {
          setHorizonPoints(s.horizonPoints.map((p, i) => ({
            id: i + 1,
            x: p.x ?? 0.5,
            y: p.y ?? 0.2
          })));
        }
        setFeatureFlags(prev => ({ ...prev, stars: s.starsEnabled ?? true }));
        if (s.features) setFeatureFlags(prev => ({ ...prev, ...s.features }));
        setStarsDensity(s.starsDensity ?? 0.5);
        setAtmosphereIntensity(s.atmosphereIntensity ?? 1.0);
        if (s.clouds) {
          setCloudSpeed(s.clouds.speed ?? 0.28);
          setCloudDensity(s.clouds.density ?? 0.46);
          setCloudIntensity(s.clouds.intensity ?? 0.3);
        }
      }

      setShowImportModal(false);
      setImportText('');
    } catch (error) {
      alert(`Failed to parse JSON: ${error.message}`);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(generatedJson);
    alert('JSON configuration copied to clipboard!');
  };

  return (
    <div className="App">
      <header className="app-header">
        <div className="brand">
          <div className="brand-icon">L</div>
          <span className="brand-name">Lumisky Visual Creator</span>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => setShowImportModal(true)}>Import Layout</button>
          <button className="btn btn-primary" onClick={() => setShowExportModal(true)}>Export JSON</button>
        </div>
      </header>

      <input 
        type="file" 
        ref={addLayerFileRef} 
        style={{ display: 'none' }} 
        accept="image/png, image/jpeg, image/webp, image/gif, image/svg+xml"
        onChange={handleAddLayerFile} 
      />

      <main className="workspace">
        {/* Left: WebGL Canvas Frame Preview */}
        <section className="canvas-panel">
          <div className="canvas-header">
            <span className="panel-title">Interactive Canvas</span>
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.8rem', cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={enableParallax} 
                  onChange={handleParallaxCheckboxChange}
                  style={{ cursor: 'pointer' }}
                />
                Parallax Effect
              </label>
              <button className="play-controls" onClick={() => setIsPlaying(!isPlaying)} title={isPlaying ? 'Stop Day Cycle' : 'Play Day Cycle'}>
                {isPlaying ? (
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="4" y="4" width="16" height="16" rx="2"/></svg>
                ) : (
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                )}
              </button>
            </div>
          </div>

          <div
            className="phone-stage"
            ref={phoneScreenRef}
            onMouseMove={handleMouseMove}
            onMouseLeave={handleMouseLeave}
            onMouseDown={handleDragStart}
            onTouchStart={handleDragStart}
            style={{ cursor: activeLayer ? 'grab' : 'default' }}
          >
            <div className="phone-frame">
              <div className="notch"></div>
            </div>
            <div className="phone-screen">
              {/* WebGL background canvas */}
              <canvas 
                ref={canvasRef} 
                width="340" 
                height="620" 
                style={{
                  width: `${workspaceScale * 100}%`,
                  height: `${workspaceScale * 100}%`,
                  display: 'block',
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)'
                }}
              />

              {/* Overlapping layers resized to cover aspect ratio */}
              {sortedLayers.map(({ layer, index }) => {
                if (layer.visible === false) return null;
                let tx = layer.offsetX;
                let ty = layer.offsetY;
                let rot = 0;
                const duration = layer.motionDuration ?? 5;
                const progress = progressFromDuration(timeState, duration);
                const pingPong = progress < 0.5 ? progress * 2 : (1 - progress) * 2;

                if (isPlaying) {
                  switch (layer.motionType) {
                    case 'PARALLAX':
                      tx += parallaxX * (layer.parallaxStrengthX ?? layer.motionAmplitude);
                      ty += parallaxY * (layer.parallaxStrengthY ?? layer.motionAmplitude);
                      break;
                    case 'BOB':
                      ty += Math.sin(timeState * layer.motionSpeed) * layer.motionAmplitude;
                      break;
                    case 'FLOAT':
                      tx += Math.sin(timeState * layer.motionSpeed) * layer.motionAmplitude;
                      break;
                    case 'WAVE': {
                      const rad = (layer.motionDirection || 0) * Math.PI / 180;
                      const wave = Math.sin(timeState * layer.motionSpeed) * layer.motionAmplitude;
                      tx += Math.cos(rad) * wave;
                      ty += Math.sin(rad) * wave;
                      break;
                    }
                    case 'SCROLL': {
                      const rad = (layer.motionDirection || 0) * Math.PI / 180;
                      tx += Math.cos(rad) * layer.motionAmplitude * progress;
                      ty += Math.sin(rad) * layer.motionAmplitude * progress;
                      break;
                    }
                    case 'POINTS':
                      tx += (layer.motionStartX ?? 0) + ((layer.motionEndX ?? 0) - (layer.motionStartX ?? 0)) * pingPong;
                      ty += (layer.motionStartY ?? 0) + ((layer.motionEndY ?? 0) - (layer.motionStartY ?? 0)) * pingPong;
                      break;
                    case 'SPIN':
                      rot = timeState * layer.motionSpeed * 57.2958;
                      break;
                  }
                } else {
                  if (layer.motionType === 'PARALLAX') {
                    tx += parallaxX * (layer.parallaxStrengthX ?? layer.motionAmplitude);
                    ty += parallaxY * (layer.parallaxStrengthY ?? layer.motionAmplitude);
                  }
                  if (layer.motionType === 'WAVE') {
                    const rad = (layer.motionDirection || 0) * Math.PI / 180;
                    const wave = Math.sin(timeState * layer.motionSpeed) * layer.motionAmplitude;
                    tx += Math.cos(rad) * wave;
                    ty += Math.sin(rad) * wave;
                  }
                }

                const tint = 1 - nightBlend * layer.nightTintFactor;
                const layerFilter = `brightness(${tint * 100}%) contrast(${100 - nightBlend * 8}%) saturate(${100 - nightBlend * 15}%)`;

                return (
                  <div 
                    key={index} 
                    className={`canvas-layer ${index === selectedIndex ? 'selected-indicator' : ''}`}
                    onMouseDown={(e) => handleDragStart(e, index)}
                    onTouchStart={(e) => handleDragStart(e, index)}
                    style={{
                      zIndex: index + 10,
                      width: `${workspaceScale * 100}%`,
                      height: `${workspaceScale * 100}%`
                    }}
                  >
                    <img 
                      src={layer.localUrl || `/${layer.texturePath}`}
                      alt={`Layer ${index}`}
                      style={{
                        transform: `translate(${tx * 100}%, ${-ty * 100}%) scale(${layer.scaleX}, ${layer.scaleY}) rotate(${rot}deg)`,
                        filter: layerFilter,
                        opacity: layer.opacity ?? 1.0
                      }}
                      onError={(e) => {
                        e.target.style.opacity = '0.3';
                      }}
                    />
                    {index === selectedIndex && (
                      <button
                        type="button"
                        className="resize-handle"
                        title="Resize layer"
                        onMouseDown={(e) => handleResizeStart(e, index)}
                        onTouchStart={(e) => handleResizeStart(e, index)}
                      />
                    )}
                  </div>
                );
              })}
              {showSafeFrame && <div className="production-frame" />}
            </div>
          </div>

        </section>

        {/* Right: Properties sidebar */}
        <section className="editor-panel">
          {/* Environmental Simulators */}
          <div className="simulation-controls">
            <div className="sim-slider-row">
              <div className="sim-slider-header">
                <span>Daytime Simulator {isPlaying && <span style={{ fontSize: '0.7rem', color: '#10b981' }}>(Auto Cycle)</span>}</span>
                <span className="sim-slider-val">{formatTime(daylightTime)}</span>
              </div>
              <input 
                type="range" 
                min="0" 
                max="24" 
                step="0.1" 
                value={daylightTime} 
                disabled={isPlaying}
                onChange={(e) => setDaylightTime(parseFloat(e.target.value))} 
              />
              <div className="snapshot-strip">
                {SNAPSHOT_TIMES.map((snapshot) => (
                  <button
                    key={snapshot.label}
                    type="button"
                    className="snapshot-chip"
                    onClick={() => {
                      setIsPlaying(false);
                      setDaylightTime(snapshot.value);
                    }}
                  >
                    <span>{snapshot.label}</span>
                    <strong>{formatTime(snapshot.value)}</strong>
                  </button>
                ))}
              </div>
            </div>

            <div className="sim-slider-row">
              <div className="sim-slider-header">
                <span>Workspace Outside Frame</span>
                <span className="sim-slider-val">{workspaceScale.toFixed(2)}x</span>
              </div>
              <input
                type="range"
                min="1"
                max="1.8"
                step="0.05"
                value={workspaceScale}
                onChange={(e) => setWorkspaceScale(parseFloat(e.target.value))}
              />
              <label className="toggle-row">
                <input
                  type="checkbox"
                  checked={showSafeFrame}
                  onChange={(e) => setShowSafeFrame(e.target.checked)}
                />
                Show dashed production frame
              </label>
            </div>

            <div className="sim-slider-row">
              <span className="sim-slider-header">Parallax Simulator {enableParallax && <span style={{ fontSize: '0.7rem', color: '#10b981' }}>(Sensors Active)</span>}</span>
              <div className="slider-container-dual">
                <div className="sim-slider-row">
                  <div className="sim-slider-header">
                    <span>Tilt X</span>
                    <span>{parallaxX.toFixed(2)}</span>
                  </div>
                  <input 
                    type="range" 
                    min="-1" 
                    max="1" 
                    step="0.05" 
                    value={parallaxX} 
                    disabled={enableParallax}
                    onChange={(e) => setParallaxX(parseFloat(e.target.value))} 
                  />
                </div>
                <div className="sim-slider-row">
                  <div className="sim-slider-header">
                    <span>Tilt Y</span>
                    <span>{parallaxY.toFixed(2)}</span>
                  </div>
                  <input 
                    type="range" 
                    min="-1" 
                    max="1" 
                    step="0.05" 
                    value={parallaxY} 
                    disabled={enableParallax}
                    onChange={(e) => setParallaxY(parseFloat(e.target.value))} 
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="editor-card">
            <div className="card-header">
              <span className="card-title">Wallpaper Target</span>
              <span className="status-pill">{exportModeLabel}</span>
            </div>
            <div className="inputs-grid">
              <div className="form-group">
                <label>Manifest ID</label>
                <input
                  type="text"
                  value={wallpaperId}
                  onChange={(e) => setWallpaperId(e.target.value.replace(/\s+/g, '_').toLowerCase())}
                />
              </div>
              <div className="form-group">
                <label>Display Name</label>
                <input
                  type="text"
                  value={wallpaperName}
                  onChange={(e) => setWallpaperName(e.target.value)}
                />
              </div>
              <div className="form-group form-group-full">
                <label>Export Format</label>
                <select value={exportMode} onChange={(e) => setExportMode(e.target.value)}>
                  <option value="manifest">Lumisky Manifest (app/assets/wallpapers/id/manifest.json)</option>
                  <option value="draft">Creator Draft (re-importable workspace)</option>
                </select>
              </div>
              <div className="form-group form-group-full">
                <label>Ready Wallpaper</label>
                <select onChange={(e) => e.target.value && loadReadyPreset(e.target.value)} defaultValue="">
                  <option value="">Select preset</option>
                  {READY_WALLPAPER_PRESETS.map((preset) => (
                    <option key={preset.id} value={preset.id}>{preset.name}</option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="editor-card">
            <div className="card-header">
              <span className="card-title">Wallpaper Features</span>
            </div>
            <div className="feature-grid">
              {[
                ['parallax', 'Parallax'],
                ['sun', 'Sun'],
                ['moon', 'Moon'],
                ['atmosphere', 'Atmosphere'],
                ['stars', 'Stars'],
                ['clouds', 'Clouds'],
                ['lensFlare', 'Lens Flare'],
                ['lowPowerThrottle', 'Power Throttle']
              ].map(([key, label]) => (
                <label key={key} className="feature-toggle">
                  <input
                    type="checkbox"
                    checked={featureFlags[key]}
                    onChange={(e) => updateFeatureFlag(key, e.target.checked)}
                  />
                  <span>{label}</span>
                </label>
              ))}
            </div>
            <div className="inputs-grid feature-settings">
              {featureFlags.atmosphere && (
                <div className="form-group form-group-full">
                  <label>Atmosphere Intensity: {atmosphereIntensity.toFixed(2)}</label>
                  <input type="range" min="0" max="1.5" step="0.05" value={atmosphereIntensity} onChange={(e) => setAtmosphereIntensity(parseFloat(e.target.value))} />
                </div>
              )}
              {featureFlags.stars && (
                <div className="form-group form-group-full">
                  <label>Stars Density: {starsDensity.toFixed(2)}</label>
                  <input type="range" min="0.1" max="1.0" step="0.05" value={starsDensity} onChange={(e) => setStarsDensity(parseFloat(e.target.value))} />
                </div>
              )}
              {featureFlags.clouds && (
                <>
                  <div className="form-group">
                    <label>Cloud Speed: {cloudSpeed.toFixed(2)}</label>
                    <input type="range" min="0" max="2" step="0.02" value={cloudSpeed} onChange={(e) => setCloudSpeed(parseFloat(e.target.value))} />
                  </div>
                  <div className="form-group">
                    <label>Cloud Density: {cloudDensity.toFixed(2)}</label>
                    <input type="range" min="0" max="1" step="0.02" value={cloudDensity} onChange={(e) => setCloudDensity(parseFloat(e.target.value))} />
                  </div>
                  <div className="form-group form-group-full">
                    <label>Cloud Intensity: {cloudIntensity.toFixed(2)}</label>
                    <input type="range" min="0" max="1" step="0.02" value={cloudIntensity} onChange={(e) => setCloudIntensity(parseFloat(e.target.value))} />
                  </div>
                </>
              )}
            </div>
          </div>

          {/* Card: Layer Manager */}
          <div className="editor-card">
            <div className="card-header">
              <span className="card-title">Scene Layers ({sceneStackItems.length})</span>
              <button className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', borderRadius: '0.5rem', fontSize: '0.8rem' }} onClick={triggerAddLayerFile}>+ Add Layer</button>
            </div>
            
            <div className="layers-list">
              {sceneStackItems.map((item, sceneIndex) => (
                <div 
                  key={item.key}
                  className={`layer-item ${item.type === 'texture' && item.textureIndex === selectedIndex ? 'active' : ''} ${item.type === 'system' ? 'system-layer' : ''} ${item.visible ? '' : 'hidden-layer'}`}
                  onClick={() => item.type === 'texture' && setSelectedIndex(item.textureIndex)}
                >
                  <label className="visibility-toggle" title={item.visible ? 'Hide layer' : 'Show layer'} onClick={(e) => e.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={item.visible}
                      onChange={(e) => {
                        if (item.type === 'texture') {
                          updateLayerAtIndex(item.textureIndex, { visible: e.target.checked });
                        } else {
                          updateFeatureFlag(item.featureKey, e.target.checked);
                        }
                      }}
                    />
                  </label>
                  <div className="layer-info" style={{ flex: 1 }}>
                    <span className="layer-name">{item.label}</span>
                    <span className="layer-sub">Index: {sceneIndex} | {item.visible ? item.detail : 'Hidden'}</span>
                  </div>
                  {item.type === 'texture' ? (
                    <div style={{ display: 'flex', gap: '0.2rem', alignItems: 'center' }}>
                      <button className="layer-actions-btn" style={{ color: '#3886e7' }} onClick={(e) => moveLayerUp(item.textureIndex, e)} disabled={item.textureIndex === 0} title="Move Up">
                        ▲
                      </button>
                      <button className="layer-actions-btn" style={{ color: '#3886e7' }} onClick={(e) => moveLayerDown(item.textureIndex, e)} disabled={item.textureIndex === layers.length - 1} title="Move Down">
                        ▼
                      </button>
                      {layers.length > 1 && (
                        <button className="layer-actions-btn" onClick={(e) => { e.stopPropagation(); deleteLayer(item.textureIndex); }} title="Delete Layer">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
                        </button>
                      )}
                    </div>
                  ) : (
                    <span className="system-layer-badge">auto</span>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Card: Selected Layer Parameters */}
          {activeLayer && (
            <div className="editor-card">
              <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span className="card-title">Edit Layer {selectedIndex} Properties</span>
                <select 
                  value={selectedIndex} 
                  onChange={(e) => setSelectedIndex(parseInt(e.target.value))}
                  style={{ width: 'auto', padding: '0.35rem 0.6rem', fontSize: '0.8rem' }}
                >
                  {layers.map((_, i) => (
                    <option key={i} value={i}>Layer {i}</option>
                  ))}
                </select>
              </div>
              
              <div className="inputs-grid">
                {/* Texture Asset Path */}
                <div className="form-group form-group-full">
                  <label>Texture Path (Assets)</label>
                  <input 
                    type="text" 
                    value={activeLayer.texturePath} 
                    onChange={(e) => updateActiveLayer('texturePath', e.target.value)} 
                  />
                </div>

                {/* Upload Local Image override */}
                <div className="form-group form-group-full">
                  <label>Replace Preview Image (PNG/GIF/SVG/WebP)</label>
                  <div className="file-input-wrapper">
                    <button className="btn btn-secondary btn-upload" style={{ width: '100%' }}>
                      Choose Graphic File
                      <input type="file" accept="image/*" onChange={handleFileUpload} />
                    </button>
                    {activeLayer.localUrl && (
                      <button className="btn btn-secondary" style={{ color: '#d6444b' }} onClick={clearActiveLayerPreview}>Clear</button>
                    )}
                  </div>
                </div>

                {/* Offsets */}
                <div className="form-group">
                  <label>Offset X: {activeLayer.offsetX.toFixed(2)}</label>
                  <input 
                    type="range" 
                    min="-2.0" 
                    max="2.0" 
                    step="0.01" 
                    value={activeLayer.offsetX} 
                    onChange={(e) => updateActiveLayer('offsetX', parseFloat(e.target.value))} 
                  />
                </div>
                <div className="form-group">
                  <label>Offset Y: {activeLayer.offsetY.toFixed(2)}</label>
                  <input 
                    type="range" 
                    min="-2.0" 
                    max="2.0" 
                    step="0.01" 
                    value={activeLayer.offsetY} 
                    onChange={(e) => updateActiveLayer('offsetY', parseFloat(e.target.value))} 
                  />
                </div>

                {/* Scales */}
                <div className="form-group">
                  <label>Scale X: {activeLayer.scaleX.toFixed(2)}</label>
                  <input 
                    type="range" 
                    min="0.1" 
                    max="3" 
                    step="0.05" 
                    value={activeLayer.scaleX} 
                    onChange={(e) => updateActiveLayer('scaleX', parseFloat(e.target.value))} 
                  />
                </div>
                <div className="form-group">
                  <label>Scale Y: {activeLayer.scaleY.toFixed(2)}</label>
                  <input 
                    type="range" 
                    min="0.1" 
                    max="3" 
                    step="0.05" 
                    value={activeLayer.scaleY} 
                    onChange={(e) => updateActiveLayer('scaleY', parseFloat(e.target.value))} 
                  />
                </div>

                {/* Opacity */}
                <div className="form-group">
                  <label>Opacity (Opaklık): {(activeLayer.opacity ?? 1.0).toFixed(2)}</label>
                  <input 
                    type="range" 
                    min="0" 
                    max="1" 
                    step="0.05" 
                    value={activeLayer.opacity ?? 1.0} 
                    onChange={(e) => updateActiveLayer('opacity', parseFloat(e.target.value))} 
                  />
                </div>

                {/* Night influence */}
                <div className="form-group form-group-full">
                  <label>Night Tint Influence: {activeLayer.nightTintFactor.toFixed(2)}</label>
                  <input 
                    type="range" 
                    min="0" 
                    max="1" 
                    step="0.05" 
                    value={activeLayer.nightTintFactor} 
                    onChange={(e) => updateActiveLayer('nightTintFactor', parseFloat(e.target.value))} 
                  />
                </div>

                {/* Motion Type */}
                <div className="form-group form-group-full" style={{ borderTop: '1px solid rgba(255,255,255,0.05)', paddingTop: '0.75rem' }}>
                  <label>Motion Type</label>
                  <select 
                    value={activeLayer.motionType} 
                    onChange={(e) => updateActiveLayer('motionType', e.target.value)}
                  >
                    <option value="STATIC">STATIC (No movement)</option>
                    <option value="PARALLAX">PARALLAX (Gyro/Scroll responds)</option>
                    <option value="BOB">BOB (Vertical float)</option>
                    <option value="FLOAT">FLOAT (Horizontal float)</option>
                    <option value="WAVE">WAVE (Directional Float Angle)</option>
                    <option value="SCROLL">SCROLL (Timed texture slide)</option>
                    <option value="POINTS">POINTS (Between two positions)</option>
                    <option value="SPIN">SPIN (Clockwise rotation)</option>
                  </select>
                </div>

                {activeLayer.motionType !== 'STATIC' && (
                  <>
                    <div className="form-group">
                      <label>Motion Speed: {(activeLayer.motionSpeed ?? 1).toFixed(1)}</label>
                      <input 
                        type="range" 
                        min="0.1" 
                        max="10" 
                        step="0.1" 
                        value={activeLayer.motionSpeed} 
                        onChange={(e) => updateActiveLayer('motionSpeed', parseFloat(e.target.value))} 
                      />
                    </div>
                    <div className="form-group">
                      <label>Motion Amount: {(activeLayer.motionAmplitude ?? 0).toFixed(3)}</label>
                      <input 
                        type="range" 
                        min="0" 
                        max="0.8" 
                        step="0.002" 
                        value={activeLayer.motionAmplitude} 
                        onChange={(e) => updateActiveLayer('motionAmplitude', parseFloat(e.target.value))} 
                      />
                    </div>
                    <div className="form-group form-group-full">
                      <label>Loop Duration: {(activeLayer.motionDuration ?? 5).toFixed(1)}s</label>
                      <input
                        type="range"
                        min="0.5"
                        max="30"
                        step="0.5"
                        value={activeLayer.motionDuration ?? 5}
                        onChange={(e) => updateActiveLayer('motionDuration', parseFloat(e.target.value))}
                      />
                    </div>
                  </>
                )}

                {['WAVE', 'SCROLL'].includes(activeLayer.motionType) && (
                  <div className="form-group form-group-full">
                    <label>Motion Direction Angle: {activeLayer.motionDirection || 0}°</label>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                      <input 
                        type="range" 
                        min="0" 
                        max="360" 
                        step="5" 
                        value={activeLayer.motionDirection || 0} 
                        onChange={(e) => updateActiveLayer('motionDirection', parseInt(e.target.value))} 
                        style={{ flex: 1 }}
                      />
                      <div 
                        style={{
                          width: '28px',
                          height: '28px',
                          borderRadius: '50%',
                          border: '2px solid #3886e7',
                          position: 'relative',
                          transform: `rotate(${activeLayer.motionDirection || 0}deg)`,
                          transition: 'transform 0.1s ease'
                        }}
                      >
                        <div style={{ width: '2px', height: '12px', background: '#3886e7', position: 'absolute', top: '2px', left: '11px' }}></div>
                      </div>
                    </div>
                  </div>
                )}

                {activeLayer.motionType === 'POINTS' && (
                  <>
                    <div className="form-group form-group-full subpanel-title">Two Point Motion</div>
                    <div className="form-group">
                      <label>Start X: {(activeLayer.motionStartX ?? 0).toFixed(2)}</label>
                      <input type="range" min="-1" max="1" step="0.01" value={activeLayer.motionStartX ?? 0} onChange={(e) => updateActiveLayer('motionStartX', parseFloat(e.target.value))} />
                    </div>
                    <div className="form-group">
                      <label>Start Y: {(activeLayer.motionStartY ?? 0).toFixed(2)}</label>
                      <input type="range" min="-1" max="1" step="0.01" value={activeLayer.motionStartY ?? 0} onChange={(e) => updateActiveLayer('motionStartY', parseFloat(e.target.value))} />
                    </div>
                    <div className="form-group">
                      <label>End X: {(activeLayer.motionEndX ?? 0).toFixed(2)}</label>
                      <input type="range" min="-1" max="1" step="0.01" value={activeLayer.motionEndX ?? 0} onChange={(e) => updateActiveLayer('motionEndX', parseFloat(e.target.value))} />
                    </div>
                    <div className="form-group">
                      <label>End Y: {(activeLayer.motionEndY ?? 0).toFixed(2)}</label>
                      <input type="range" min="-1" max="1" step="0.01" value={activeLayer.motionEndY ?? 0} onChange={(e) => updateActiveLayer('motionEndY', parseFloat(e.target.value))} />
                    </div>
                  </>
                )}

                {activeLayer.motionType === 'PARALLAX' && (
                  <>
                    <div className="form-group">
                      <label>Parallax X: {(activeLayer.parallaxStrengthX ?? 0.05).toFixed(3)}</label>
                      <input type="range" min="0" max="0.4" step="0.005" value={activeLayer.parallaxStrengthX ?? 0.05} onChange={(e) => updateActiveLayer('parallaxStrengthX', parseFloat(e.target.value))} />
                    </div>
                    <div className="form-group">
                      <label>Parallax Y: {(activeLayer.parallaxStrengthY ?? 0.03).toFixed(3)}</label>
                      <input type="range" min="0" max="0.4" step="0.005" value={activeLayer.parallaxStrengthY ?? 0.03} onChange={(e) => updateActiveLayer('parallaxStrengthY', parseFloat(e.target.value))} />
                    </div>
                  </>
                )}
              </div>
            </div>
          )}

          {/* Card: Background & GLSL Editor */}
          <div className="editor-card">
            <div className="card-header">
              <span className="card-title">Background & Celestial GLSL Editor</span>
            </div>

            <div className="inputs-grid">
              {/* Sun Config */}
              <div className="form-group form-group-full" style={{ borderBottom: '1px solid rgba(255,255,255,0.03)', paddingBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#3886e7' }}>Sun Properties</span>
              </div>
              <div className="form-group">
                <label>Sun Motion Path</label>
                <select value={sunMotionType} onChange={(e) => setSunMotionType(e.target.value)}>
                  <option value="ARCH">ARCH (Orbiting curve)</option>
                  <option value="LINEAR">LINEAR (Vertical line)</option>
                  <option value="STATIC">STATIC (Locked position)</option>
                </select>
              </div>
              <div className="form-group">
                <label>Sun Color</label>
                <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                  <input type="color" value={sunColor} onChange={(e) => setSunColor(e.target.value)} style={{ padding: 0, width: '24px', height: '24px', border: 'none', background: 'transparent', cursor: 'pointer' }} />
                  <input type="text" value={sunColor} onChange={(e) => setSunColor(e.target.value)} style={{ flex: 1, padding: '0.25rem 0.5rem', fontSize: '0.8rem' }} />
                </div>
              </div>
              <div className="form-group">
                <label>Sun Radius Size: {sunSize.toFixed(3)}</label>
                <input type="range" min="0.01" max="0.2" step="0.005" value={sunSize} onChange={(e) => setSunSize(parseFloat(e.target.value))} />
              </div>
              <div className="form-group">
                <label>Sun Glow Intensity: {sunGlow.toFixed(2)}</label>
                <input type="range" min="0.0" max="1.5" step="0.05" value={sunGlow} onChange={(e) => setSunGlow(parseFloat(e.target.value))} />
              </div>
              <div className="form-group">
                <label>Sun X / Line Anchor: {sunStaticX.toFixed(2)}</label>
                <input type="range" min="0" max="1" step="0.01" value={sunStaticX} onChange={(e) => setSunStaticX(parseFloat(e.target.value))} />
              </div>
              <div className="form-group">
                <label>Sun Y / Static Height: {sunStaticY.toFixed(2)}</label>
                <input type="range" min="0" max="1" step="0.01" value={sunStaticY} onChange={(e) => setSunStaticY(parseFloat(e.target.value))} />
              </div>
              <div className="form-group form-group-full">
                <label>Sun Max Altitude (Zenith Y): {sunZenith.toFixed(2)}</label>
                <input type="range" min="0.1" max="0.9" step="0.02" value={sunZenith} onChange={(e) => setSunZenith(parseFloat(e.target.value))} />
              </div>

              {/* Moon Config */}
              <div className="form-group form-group-full" style={{ borderBottom: '1px solid rgba(255,255,255,0.03)', paddingBottom: '0.5rem', paddingTop: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#eceff1' }}>Moon Properties</span>
              </div>
              <div className="form-group">
                <label>Moon Motion Path</label>
                <select value={moonMotionType} onChange={(e) => setMoonMotionType(e.target.value)}>
                  <option value="ARCH">ARCH (Orbiting curve)</option>
                  <option value="LINEAR">LINEAR (Vertical line)</option>
                  <option value="STATIC">STATIC (Locked position)</option>
                </select>
              </div>
              <div className="form-group">
                <label>Moon Color</label>
                <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                  <input type="color" value={moonColor} onChange={(e) => setMoonColor(e.target.value)} style={{ padding: 0, width: '24px', height: '24px', border: 'none', background: 'transparent', cursor: 'pointer' }} />
                  <input type="text" value={moonColor} onChange={(e) => setMoonColor(e.target.value)} style={{ flex: 1, padding: '0.25rem 0.5rem', fontSize: '0.8rem' }} />
                </div>
              </div>
              <div className="form-group">
                <label>Moon Radius Size: {moonSize.toFixed(3)}</label>
                <input type="range" min="0.01" max="0.2" step="0.005" value={moonSize} onChange={(e) => setMoonSize(parseFloat(e.target.value))} />
              </div>
              <div className="form-group">
                <label>Moon Glow Intensity: {moonGlow.toFixed(2)}</label>
                <input type="range" min="0.0" max="1.5" step="0.05" value={moonGlow} onChange={(e) => setMoonGlow(parseFloat(e.target.value))} />
              </div>
              <div className="form-group">
                <label>Moon X / Line Anchor: {moonStaticX.toFixed(2)}</label>
                <input type="range" min="0" max="1" step="0.01" value={moonStaticX} onChange={(e) => setMoonStaticX(parseFloat(e.target.value))} />
              </div>
              <div className="form-group">
                <label>Moon Y / Static Height: {moonStaticY.toFixed(2)}</label>
                <input type="range" min="0" max="1" step="0.01" value={moonStaticY} onChange={(e) => setMoonStaticY(parseFloat(e.target.value))} />
              </div>
              <div className="form-group form-group-full">
                <label>Moon Max Altitude (Zenith Y): {moonZenith.toFixed(2)}</label>
                <input type="range" min="0.1" max="0.9" step="0.02" value={moonZenith} onChange={(e) => setMoonZenith(parseFloat(e.target.value))} />
              </div>

              {/* Skyline Horizon Points Config */}
              <div className="form-group form-group-full" style={{ borderBottom: '1px solid rgba(255,255,255,0.03)', paddingBottom: '0.5rem', paddingTop: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#10b981' }}>Skyline Horizon Points ({horizonPoints.length}/16)</span>
                  <button className="btn btn-primary" style={{ padding: '0.2rem 0.5rem', borderRadius: '0.35rem', fontSize: '0.75rem' }} onClick={addHorizonPoint}>+ Add Point</button>
                </div>
              </div>

              <div className="form-group form-group-full" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '200px', overflowY: 'auto', paddingRight: '0.2rem' }}>
                {sortedHorizonPoints.map((pt, idx) => (
                  <div 
                    key={pt.id} 
                    style={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      gap: '0.5rem', 
                      background: editingHorizonId === pt.id ? 'rgba(16, 185, 129, 0.1)' : 'rgba(255,255,255,0.02)',
                      border: '1px solid rgba(255,255,255,0.05)',
                      padding: '0.5rem',
                      borderRadius: '0.5rem'
                    }}
                    onClick={() => setEditingHorizonId(pt.id)}
                  >
                    <span style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#8f96a9', width: '60px' }}>Pt {idx + 1}</span>
                    <div style={{ flex: 1, display: 'flex', gap: '0.5rem' }}>
                      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
                        <span style={{ fontSize: '0.65rem', color: '#8f96a9' }}>X: {pt.x.toFixed(2)}</span>
                        <input 
                          type="range" 
                          min="0" 
                          max="1" 
                          step="0.01" 
                          value={pt.x} 
                          onChange={(e) => updateHorizonPoint(pt.id, 'x', parseFloat(e.target.value))} 
                        />
                      </div>
                      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
                        <span style={{ fontSize: '0.65rem', color: '#8f96a9' }}>Height Y: {pt.y.toFixed(2)}</span>
                        <input 
                          type="range" 
                          min="0.01" 
                          max="0.6" 
                          step="0.01" 
                          value={pt.y} 
                          onChange={(e) => updateHorizonPoint(pt.id, 'y', parseFloat(e.target.value))} 
                        />
                      </div>
                    </div>
                    {horizonPoints.length > 2 && (
                      <button className="layer-actions-btn" onClick={(e) => { e.stopPropagation(); deleteHorizonPoint(pt.id); }} style={{ padding: 0 }}>
                        &times;
                      </button>
                    )}
                  </div>
                ))}
              </div>

            </div>

            {/* GLSL Code editor section */}
            <div className="form-group form-group-full" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginTop: '0.5rem' }}>
              <label>WebGL Fragment Shader (GLSL)</label>
              <textarea 
                value={glslCode} 
                onChange={(e) => setGlslCode(e.target.value)} 
                className="glsl-code-editor"
                spellCheck="false"
              />
              <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                <button className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem' }} onClick={() => setGlslCode(DEFAULT_GLSL_SHADER)}>Reset Shader</button>
                <button className="btn btn-primary" style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem' }} onClick={() => {
                  const canvas = canvasRef.current;
                  if (canvas) {
                    const gl = canvas.getContext('webgl');
                    if (gl) compileWebGLShaders(gl, glslCode);
                  }
                }}>Compile & Apply</button>
              </div>
              {shaderError ? (
                <div className="shader-console shader-console-error">
                  <strong>Compiler Error:</strong>
                  <pre>{shaderError}</pre>
                </div>
              ) : (
                <div className="shader-console shader-console-success">
                  WebGL program compiled successfully!
                </div>
              )}
            </div>
          </div>
        </section>
      </main>

      {/* Dialog: Export Layout */}
      {showExportModal && (
        <div className="modal-overlay" onClick={() => setShowExportModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Export {exportModeLabel} JSON</span>
              <button className="modal-close" onClick={() => setShowExportModal(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <div className="export-mode-toggle">
                <button
                  type="button"
                  className={`mode-btn ${exportMode === 'manifest' ? 'active' : ''}`}
                  onClick={() => setExportMode('manifest')}
                >
                  Lumisky Manifest
                </button>
                <button
                  type="button"
                  className={`mode-btn ${exportMode === 'draft' ? 'active' : ''}`}
                  onClick={() => setExportMode('draft')}
                >
                  Creator Draft
                </button>
              </div>
              <p style={{ fontSize: '0.85rem', color: '#8f96a9' }}>
                {exportMode === 'manifest'
                  ? 'Copy this JSON into the selected wallpaper manifest asset file.'
                  : 'Copy this draft when you want to reopen the same creator workspace later.'}
              </p>
              <textarea 
                className="json-textarea" 
                readOnly 
                value={generatedJson}
                onClick={(e) => e.target.select()}
              />
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowExportModal(false)}>Close</button>
              <button className="btn btn-primary" onClick={copyToClipboard}>Copy to Clipboard</button>
            </div>
          </div>
        </div>
      )}

      {/* Dialog: Import Layout */}
      {showImportModal && (
        <div className="modal-overlay" onClick={() => setShowImportModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Import Wallpaper Configuration JSON</span>
              <button className="modal-close" onClick={() => setShowImportModal(false)}>&times;</button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: '0.85rem', color: '#8f96a9' }}>Paste your exported layout JSON structure below to load the layer settings into the workspace.</p>
              <textarea 
                className="json-textarea" 
                placeholder='{ "layers": [...] }'
                value={importText}
                onChange={(e) => setImportText(e.target.value)}
              />
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowImportModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleImport}>Import Layout</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
