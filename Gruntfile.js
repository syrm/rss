/*global module:false*/
module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    // Task configuration.
    coffee: {
      compile: {
        files: {
          'assets_tmp/all_coffee.js': 'app/assets/javascripts/*.coffee'
        }
      }
    },
    concat: {
      dist: {
        src: [
          'public/vendor/bootstrap/js/bootstrap.js',
          'public/vendor/bootstrap.file-input.js',
          'public/vendor/jquery.scrollTo.js',
          'public/vendor/hyphenator/hyphenator.js',
          'assets_tmp/all_coffee.js'
        ],
        dest: 'assets_tmp/all.js'
      }
    },
    uglify: {
      dist: {
        src: 'assets_tmp/all.js',
        dest: 'public/javascripts/all.min.js'
      }
    },
    less: {
      compile: {
        options: {
          yuicompress: true
        },
        files: {
          'assets_tmp/all_less.css': 'app/assets/stylesheets/*.less'
        }
      }
    },
    cssmin: {
      dist: {
        files: {
          'assets_tmp/all.min.css': [
            'public/vendor/bootstrap/css/bootstrap.css',
            'public/vendor/bootstrap/css/bootstrap-responsive.css',
            'public/vendor/Font-Awesome/css/font-awesome.css',
            'assets_tmp/all_less.css'
          ]
        }
      }
    },
    'string-replace': {
      fontAwesome: {
        options: {
          replacements: [{
            pattern: /\.\.\/font\/fontawesome-webfont/ig,
            replacement: '/assets/vendor/Font-Awesome/font/fontawesome-webfont'
          }]
        },
        files: {
          'public/stylesheets/all.min.css': 'assets_tmp/all.min.css'
        }
      }
    },
    compress: {
      dist: {
        options: {
          mode: 'gzip'
        },
        files: [
          { src: 'public/stylesheets/all.min.css', dest: 'public/stylesheets/all.min.css.gz' },
          { src: 'public/javascripts/all.min.js', dest: 'public/javascripts/all.min.js.gz' }
        ]
      }
    },
    clean: ["assets_tmp" ],
    watch: {
      scripts: {
        files: [
          'app/assets/javascripts/*.coffee',
          'app/assets/stylesheets/*.less'
        ],
        tasks: ['default']
      }
    }
  });

  // These plugins provide necessary tasks.
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-coffee');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-string-replace');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-compress');

  // Default task.
  grunt.registerTask('default', [
    'coffee', 'concat', 'uglify', 'less', 'cssmin',
    'string-replace:fontAwesome', 'compress', 'clean']);

};
