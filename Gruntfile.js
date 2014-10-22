module.exports = function(grunt) {

    grunt.initConfig({
	pkg: grunt.file.readJSON('package.json'),
	bower: {
	    install: {
		options: {
		    targetDir: './bower_deps',
		    layout: 'byComponent',
		    install: true,
		    verbose: false,
		    cleanTargetDir: true
		}
	    }
	},
	react: {
	    build: {
		files: [{
		    expand: true,
		    cwd: './src-jsx',
		    src: '*.jsx',
		    dest: 'resources/public/dev/jsxc/',
		    ext: '.js'
		}]
	    }
	},
	watch: {
	    react: {
		files: ['src-jsx/*.jsx'],
		tasks: ['react:build', 'closureDepsWriter:dev']
	    },
	    deps: {
		files: ['src-js/**/*.js', 'src-js/*.js'],
		tasks: ['closureDepsWriter:dev']
	    }
	},
	compass: {
	    options: {
		config: 'compass-config.rb',
	    },
	    watch: {
		options: {
		    watch: true
		}
	    },
	    dev: {},
	    release: {
		options: {
		    cssDir: 'resources/public/min',
		    environment: 'production',
		    outputStyle: 'compressed'
		}
	    }
	},
	concurrent: {
	    watch: {
		tasks: ['compass', 'watch'],
		options: {
		    logConcurrentOutput: true
		}
	    }
	},
	closureDepsWriter: {
	    dev: {
		dest: 'resources/public/dev/deps.js',
		options: {
		    closureLibraryPath: 'bower_components/closurelibrary/',
		    root_with_prefix: ['"src-js ../js"',
				       '"resources/public/dev/jsxc ../jsxc"']
		},
	    }
	},
	closureBuilder: {
	    app: {
		options: {
		    inputs: 'src-js/app.js',
		    closureLibraryPath: 'bower_components/closurelibrary/',
		    compilerFile: 'bower_components/closure-compiler/lib/vendor/compiler.jar',
		    compile: true,
		    compilerOpts: {
			compilation_level: 'SIMPLE_OPTIMIZATIONS',
			externs: ['bower_components/react-externs/externs.js'],
			define: ["'goog.DEBUG=false'"]
		    }
		},
		src: ['src-js',
		      'resources/public/dev/jsxc',
		      'bower_components/closurelibrary'],
		dest: 'resources/public/min/app.min.js'
	    }
	}
    });

    grunt.loadNpmTasks('grunt-bower-task');
    grunt.loadNpmTasks('grunt-concurrent');
    grunt.loadNpmTasks('grunt-contrib-compass');
    grunt.loadNpmTasks('grunt-react');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-closure-tools');

    grunt.registerTask('deps', ['closureDepsWriter:dev']);
    
    grunt.registerTask('default', ['bower:install',
				   'compass:dev',
				   'react:build',
				   'closureDepsWriter:dev',
				   'concurrent:watch']);

    grunt.registerTask('dev', ['bower:install',
			       'compass:dev',
			       'react:build',
			       'closureDepsWriter:dev']);
    
    grunt.registerTask('release', ['bower:install',
				   'compass:release',
				   'react:build',
				   'closureBuilder']);
};
