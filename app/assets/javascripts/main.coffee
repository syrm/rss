jQuery ->
  $(window).on 'keypress', (event)->
    key = String.fromCharCode(event.which)
    switch key
      when 'j' then $('ul.items').find('.selected').next().trigger('click')
      when 'k' then $('ul.items').find('.selected'). prev().trigger('click')

  $('ul.items').on
    click: (event)->
      if $(event.target).hasClass('item')
        $('ul.items').find('.selected').removeClass('selected')
        $(event.target).addClass('read').addClass('selected')
        $(':animated').stop()
        $.get '/item/' + $(event.target).attr('id').replace('item_', ''), null, (data)->
          $('section.article').html(data)
          Hyphenator.run()

        $(window).scrollTo 0, 0
