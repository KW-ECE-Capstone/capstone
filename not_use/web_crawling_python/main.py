from tkinter import *
from tkinter import filedialog
import PIL.Image, PIL.ImageTk
from ffpyplayer.player import MediaPlayer # pip install ffpyplayer
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from bs4 import BeautifulSoup
import pandas as pd
from IPython.display import display
from webdriver_manager.chrome import ChromeDriverManager
import warnings
warnings.filterwarnings(action='ignore')
import cv2
import pytube
from moviepy.editor import *
import time

class videoGUI:
    def __init__(self, window, window_title):
        self.window = window
        self.window.title(window_title)
        self.url = self.get_video()
        self.crawling(self.url)
        top_frame = Frame(self.window)
        top_frame.pack(side=TOP, pady=5)
        bottom_frame = Frame(self.window)
        bottom_frame.pack(side=BOTTOM, pady=5)
        self.cap = None
        self.pause = False   # Parameter that controls pause button
        self.delay = 15   # ms
        self.canvas = Canvas(top_frame)
        self.canvas.pack()
        self.open_file()

        self.btn_play = Button(bottom_frame, text="Advance", width=15, command=self.advance_video)
        self.btn_play.grid(row=0, column=0)

        # Play Button
        self.btn_play=Button(bottom_frame, text="Play", width=15, command=self.play_video)
        self.btn_play.grid(row=0, column=1)

        # Pause Button
        self.btn_pause=Button(bottom_frame, text="Pause", width=15, command=self.pause_video)
        self.btn_pause.grid(row=0, column=2)
        self.window.mainloop()

    def open_file(self):
        self.pause = True
        self.elaspedTime=0
        self.totalElaspedTime=0
        self.filename = 'C:\youtube\\test.mp4'
        #audio, sr = librosa.load(self.filename+".wav")
        #librosa.display.waveplot(audio, sr=sr)
        #plt.title('draw waveform')
        #plt.show()

        # Open the video file
        self.cap = cv2.VideoCapture(self.filename)
        self.width = self.cap.get(cv2.CAP_PROP_FRAME_WIDTH)
        self.height = self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT)
        self.canvas.config(width = self.width, height = self.height)
        self.fps = self.cap.get(cv2.CAP_PROP_FPS)
        self.total_frame = int(self.cap.get(cv2.CAP_PROP_FRAME_COUNT))
        self.total_sec = self.total_frame / self.fps
        self.test = 10 / self.total_sec
        ff_opts = {'paused':True, 'vn':True}  # only audio
        self.aux = MediaPlayer(self.filename, ff_opts=ff_opts, callback= self.audioCallback)
        pass

    def get_videoFrame(self):   # get only one frame
        try:
            if self.cap.isOpened():
                self.elapsedTime=  time.time()- self.start_time #sec
                seekVal= int( self.fps*(self.totalElaspedTime+self.elapsedTime))
                self.cap.set(cv2.CAP_PROP_POS_FRAMES, seekVal)
                ret, frame = self.cap.read()
                if frame is None:
                    return (False, None)
                return (ret, cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        except Exception as e:
            print(e)
            return (False, None)

    def play_video(self):
        print("play_video...")
        print('Position:', int(self.cap.get(cv2.CAP_PROP_POS_FRAMES)))
        if self.pause:
            self.pause= False
            self.start_time = time.time()
            self.play_loop()
        pass

    def pause_video(self):
        print("pause_video...")
        if self.pause:
            return
        self.pause= True
        self.aux.set_pause(self.pause)  # Stop the audio
        self.totalElaspedTime = self.totalElaspedTime + self.elapsedTime

    def advance_video(self):
        print("advance_video...")

        self.totalElaspedTime = self.totalElaspedTime + self.elapsedTime + 10
        self.aux.seek(self.test * self.aux.get_metadata()['duration'], relative=False)
        self.play_video()

        pass


    def play_loop(self):
        if self.pause:
            return
        self.aux.set_pause(self.pause)  # Start/Stop the audio
        audio_frame, val = self.aux.get_frame()
        ret, video_frame = self.get_videoFrame()
        if ret:
            self.photo = PIL.ImageTk.PhotoImage(image = PIL.Image.fromarray(video_frame))
            self.canvas.create_image(0, 0, image = self.photo, anchor = NW)
        else:
            return
        self.after_id = self.window.after(self.delay, self.play_loop)
        pass

    def crawling(self, url):
        yt = pytube.YouTube(url)

        videos = yt.streams.all()

        yt.streams.filter(progressive=True, file_extension='mp4').order_by('resolution').desc().first().download(
            'C:\youtube', 'test.mp4')

    def get_video(self):
        feature = input('검색어를 입력하시오 : ')

        driver = webdriver.Chrome(ChromeDriverManager().install())
        driver.get('https://www.youtube.com')

        n = 3
        while n > 0:
            print('웹페이지를 불러오는 중입니다..' + '..' * n)
            time.sleep(1)
            n -= 1

        src = driver.find_element_by_name("search_query")
        src.send_keys(feature)
        src.send_keys(Keys.RETURN)

        n = 2
        while n > 0:
            print('검색 결과를 불러오는 중입니다..' + '..' * n)
            time.sleep(1)
            n -= 1

        print('데이터 수집 중입니다....')

        html = driver.page_source
        soup = BeautifulSoup(html)

        df_title = []
        df_link = []
        df_writer = []
        df_view = []
        df_date = []

        for i in range(len(soup.find_all('ytd-video-meta-block', 'style-scope ytd-video-renderer byline-separated'))):
            title = soup.find_all('a', {'id': 'video-title'})[i].text.replace('\n', '')
            link = 'https://www.youtube.com/' + soup.find_all('a', {'id': 'video-title'})[i]['href']
            writer = \
                soup.find_all('ytd-channel-name', 'long-byline style-scope ytd-video-renderer')[i].text.replace('\n',
                                                                                                                '').split(
                    ' ')[0]
            view = \
                soup.find_all('ytd-video-meta-block', 'style-scope ytd-video-renderer byline-separated')[i].text.split(
                    '•')[
                    1].split('\n')[3]
            date = \
                soup.find_all('ytd-video-meta-block', 'style-scope ytd-video-renderer byline-separated')[i].text.split(
                    '•')[
                    1].split('\n')[4]

            df_title.append(title)
            df_link.append(link)
            df_writer.append(writer)
            df_view.append(view)
            df_date.append(date)

        df_just_video = pd.DataFrame(columns=['영상제목', '채널명', '영상url', '조회수', '영상등록날짜'])

        df_just_video['영상제목'] = df_title
        df_just_video['채널명'] = df_writer
        df_just_video['영상url'] = df_link
        df_just_video['조회수'] = df_view
        df_just_video['영상등록날짜'] = df_date

        df_just_video.to_csv('C:\\youtube\\df_just_video.csv', encoding='utf-8-sig', index=False)

        driver.close()

        result = input('데이터프레임 저장이 완료되었습니다! 데이터프레임을 조회하시겠습니까? (y/n)')

        if result == 'y':
            display(df_just_video)
            question = input('원하는 영상을 재생하시겠습니까? (y/n)')
            if question == 'y':
                button = int(input('재생하고자 하는 영상의 번호(출력된 표 가장 왼쪽의 번호)를 입력해주세요.'))
                url = df_just_video['영상url'][button]
                return url

            else:
                exit()
        else:
            exit()



    def __del__(self):
        if self.cap==None:
            return
        if self.cap.isOpened():
            self.cap.release()

    def audioCallback(self,selector, value):
        self.aux.set_pause(True)  # Stop the audio
        if selector=="eof":
            pass
    pass

pass #End of Class



if __name__ == "__main__":
    videoGUI(Tk(), "My Window Title")
    pass