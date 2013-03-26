jQuery ->
  $(window).on 'keypress', (event)->
    key = String.fromCharCode(event.which)
    console.log('key', key)
    switch key
      when 'j' then $('.items').find('li.open').next().trigger('click')
      when 'k' then $('.items').find('li.open').prev().trigger('reverseClick')

  $('ul.items').on
    click: (event)->
      if $(event.target).hasClass('item')
        $('.items').find('li.open').addClass('toClose').addClass('read')
        $(event.target).addClass('open')
        $.get('/item/' + $(event.target).attr('id').replace('item_', '') + '/read')
        $(window).scrollTo $(event.target), 400,
          offset: - $('.navbar').height()
          onAfter: ->
            $('.items').find('li.toClose').removeClass('open').removeClass('toClose')
            $(window).scrollTo($(event.target), 0, { offset: - $('.navbar').height() })

    reverseClick: (event)->
      if $(event.target).hasClass('item')
        $('.items').find('li.open').addClass('toClose').addClass('read')
        $(window).scrollTo($(event.target), 0, { offset: - $('.navbar').height() })
        $(event.target).css({height: '30px', overflow: 'hidden'})
        $(event.target).addClass('open')
        $(event.target).animate {height: $(event.target).find('.article').height()},
          duration: 500
          progress: ->
            $(window).scrollTo($(event.target), 0, { offset: - $('.navbar').height() })
          complete: ->
            $(event.target).css({height: 'auto', overflow: 'auto'})
            $('.items').find('li.toClose').css({overflow: 'hidden'})
            $('.items').find('li.toClose').animate {height: 30}, 500, 'swing', ->
              $('.items').find('li.toClose').css({height: 'auto', overflow: 'auto'})
              $('.items').find('li.toClose').removeClass('open').removeClass('toClose')
